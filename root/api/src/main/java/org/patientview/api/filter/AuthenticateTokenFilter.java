package org.patientview.api.filter;

import com.google.common.util.concurrent.RateLimiter;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.LookingLocalService;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to get the filter from the request. Lookup the token and add the user into the security context.
 *
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@WebFilter(urlPatterns = { "*" }, filterName = "authenticationTokenFilter")
public class AuthenticateTokenFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateTokenFilter.class);

    private AuthenticationService authenticationService;

    private List<String> publicUrls = new ArrayList<>();

    private static Map<Long, RateLimiter> rateLimiterMap;

    /**
     * Set up publicly available URLs.
     */
    @PostConstruct
    public void init() {
        LOG.info("Authentication token filter initialised");

        // all users login/logout/error/forgotten password
        publicUrls.add("/auth/login");
        publicUrls.add("/auth/logout");
        publicUrls.add("/auth/forgottenpassword");
        publicUrls.add("/auth/userinformation");
        publicUrls.add("/error");

        // importer user login
        publicUrls.add("/import/login");

        // mobile app login
        publicUrls.add("/auth/loginmobile");

        // public news
        publicUrls.add("/public/news");

        // patient join requests
        publicUrls.add("/public/group");
        publicUrls.add("/public/request");

        // patient password request (contact unit)
        publicUrls.add("/public/passwordrequest");

        // verify email
        publicUrls.add("/verify");

        // used for ECS
        publicUrls.add("/ecs/getpatientidentifiers");

        // status for external info
        publicUrls.add("/status");

        // Swagger.io API documentation
        publicUrls.add("/api-docs");

        // Looking Local
        publicUrls.add("/lookinglocal/home");
        publicUrls.add("/lookinglocal/auth");

        // GP account creation
        publicUrls.add("/gp/validatedetails");
        publicUrls.add("/gp/claim");

        // external conversation creation
        publicUrls.add("/conversations/external");

        for (String publicUrl : this.publicUrls) {
            LOG.info("publicUrls: " + publicUrl);
        }

        // to store RateLimiter
        rateLimiterMap = new HashMap<>();
    }

    /**
     * Check if path is publicly available (no login required).
     * @param path String path to check is publicly available
     * @return true if publicly available, false if not
     */
    private boolean isPublicPath(String path) {
        for (String publicUrl : this.publicUrls) {
            if (path.contains(publicUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is the method to authorize the user to use the service in spring.
     * @param request servlet request
     * @param response servlet response
     * @param chain filter chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = this.getAsHttpRequest(request);
        String path = httpRequest.getRequestURI();

        // TODO Fix for Spring Boot bug with using delegating proxy
        setAuthenticationManager(request);

        //LOG.info(path);
        //LOG.info(String.valueOf(isPublicPath(path)));

        // Fix for CORS not required for PROD
        if (httpRequest.getMethod().equalsIgnoreCase("options")) {
            chain.doFilter(request, response);
        } else if (isPublicPath(path)) {
            chain.doFilter(request, response);
        } else {
            if (!authenticateRequest(httpRequest)) {
                redirectFailedAuthentication((HttpServletResponse) response, request, path);
                return;
            }
            chain.doFilter(request, response);
        }
    }

    /**
     * Set the authentication in the security context, returning false if authentication request fails due to session
     * expiration (will log out if expired) or failed authentication due to incorrect authentication token.
     * @param httpServletRequest HttpServletRequest
     * @return true if request is authenticated, false if not
     */
    private boolean authenticateRequest(HttpServletRequest httpServletRequest) {
        String authToken = this.extractAuthTokenFromRequest(httpServletRequest);
        PreAuthenticatedAuthenticationToken authenticationToken =
                new PreAuthenticatedAuthenticationToken(authToken, authToken);

        try {
            if (authenticationService.sessionExpired(authToken)) {
                authenticationService.logout(authToken, true);
                return false;
            }

            Authentication authentication = authenticationService.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // rate limiting by UserToken.rateLimit, set in AuthenticationService.authenticate()
            UserToken userToken = (UserToken) authentication.getCredentials();

            if (userToken.getRateLimit() != null) {
                User user = (User) authentication.getPrincipal();
                RateLimiter rateLimiter = rateLimiterMap.get(user.getId());
                if (rateLimiter == null) {
                    rateLimiter = RateLimiter.create(userToken.getRateLimit());
                    rateLimiterMap.put(user.getId(), rateLimiter);
                }
                rateLimiter.acquire();
            }

            return true;
        } catch (AuthenticationServiceException e) {
            //LOG.info("Authentication failed for " + authenticationToken.getName() + ", " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends unauthorised response to client if authentication has failed.
     * @param response HttpServletResponse passed in to send error
     */
    private void redirectFailedAuthentication(
            HttpServletResponse response, ServletRequest servletRequest, String path) {

        try {
            if (path.contains("/lookinglocal/")) {
                // send Looking Local XML error page as is a Looking Local request
                WebApplicationContext webApplicationContext =
                        WebApplicationContextUtils.getWebApplicationContext(servletRequest.getServletContext());

                LookingLocalService lookingLocalService
                        = (LookingLocalService) webApplicationContext.getBean("lookingLocalServiceImpl");

                try {
                    String errorXml = lookingLocalService.getErrorXml("Unauthorised");
                    response.setContentType("text/xml");
                    response.setContentLength(errorXml.length());
                    PrintWriter out;
                    out = response.getWriter();
                    out.println(errorXml);
                    out.close();
                    out.flush();
                } catch (TransformerException | ParserConfigurationException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorised");
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorised");
            }
        } catch (IOException ioe) {
            LOG.error("Could not send 401 for " + path);
            throw new RuntimeException("Error sending 401 for unauthorised request for " + path);
        }
    }

    /**
     * Set authentication manager, only required due to Spring Boot bug with delegating proxy.
     * @param servletRequest ServletRequest
     */
    private void setAuthenticationManager(ServletRequest servletRequest) {
        if (authenticationService == null) {
            WebApplicationContext webApplicationContext =
                    WebApplicationContextUtils.getWebApplicationContext(servletRequest.getServletContext());
            authenticationService = (AuthenticationService) webApplicationContext.getBean("authenticationServiceImpl");
        }
    }

    /**
     * Convert ServletRequest to HttpServletRequest, throwing exception if not a HTTP request.
     * @param request ServletRequest to convert to HttpServletRequest
     * @return HttpServletRequest converted from ServletRequest
     */
    private HttpServletRequest getAsHttpRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }
        return (HttpServletRequest) request;
    }

    /**
     * Retrieve the String authentication token from a request, either as a header or if header not found, from a
     * request parameter.
     * @param httpRequest HttpServletRequest to extract authentication token from
     * @return String authentication token
     */
    private String extractAuthTokenFromRequest(HttpServletRequest httpRequest) {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        if (authToken == null) {
            authToken = httpRequest.getParameter("token");
        }

        return authToken;
    }
}
