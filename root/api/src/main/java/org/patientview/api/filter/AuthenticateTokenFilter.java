package org.patientview.api.filter;

import org.patientview.api.service.AuthenticationService;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Class to get the filter from the request. Lookup the token and add the user into the security context
 *
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@WebFilter(urlPatterns = { "*" }, filterName = "authenticationTokenFilter")
public class AuthenticateTokenFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateTokenFilter.class);

    private AuthenticationService authenticationService;

    private List<String> publicUrls = new ArrayList<>();

    @PostConstruct
    public void init() {
        LOG.info("Authentication token filter initialised");

        // all users login/logout/error
        publicUrls.add("/auth/login");
        publicUrls.add("/auth/logout");
        publicUrls.add("/error");

        // public news
        publicUrls.add("/public/news");

        // patient join requests
        publicUrls.add("/public/group");
        publicUrls.add("/public/joinrequest");

        // patient password request (contact unit)
        publicUrls.add("/public/passwordrequest");

        for (String publicUrl : this.publicUrls) {
            LOG.info("publicUrls: " + publicUrl);
        }
    }

    private boolean isPublicPath(String path) {
        for (String publicUrl : this.publicUrls) {
            if (path.contains(publicUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is the method to authorize the user to use the service is spring.
     *
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

        // Fix for CORS not required for PROD
        if (httpRequest.getMethod().equalsIgnoreCase("options")) {
            chain.doFilter(request, response);
        } else if (isPublicPath(path)) {
            chain.doFilter(request, response);
        } else {
            if (!authenticateRequest(httpRequest)) {
                LOG.info("Request is not authenticated");
                redirectFailedAuthentication((HttpServletResponse) response);
            }
            chain.doFilter(request, response);
        }
    }

    // Set the authentication in the security context
    private boolean authenticateRequest(HttpServletRequest httpServletRequest) {
        String authToken = this.extractAuthTokenFromRequest(httpServletRequest);
        PreAuthenticatedAuthenticationToken authenticationToken =
                new PreAuthenticatedAuthenticationToken(authToken, authToken);

        try {
            Authentication authentication = authenticationService.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (AuthenticationServiceException e) {
            LOG.info("Authentication failed for " + authenticationToken.getName() + ", " + e.getMessage());
            return false;
        }
    }

    private void redirectFailedAuthentication(HttpServletResponse response) {
        try {
            LOG.info("Failed Authentication");
            response.sendRedirect("/api/error");
        } catch (IOException ioe) {
            LOG.error("Could not redirect response");
            throw new RuntimeException("Error redirecting unauthorised request");
        }
    }

    private void setAuthenticationManager(ServletRequest servletRequest) {
        if (authenticationService == null) {
            WebApplicationContext webApplicationContext =
                    WebApplicationContextUtils.getWebApplicationContext(servletRequest.getServletContext());
            authenticationService = (AuthenticationService) webApplicationContext.getBean("authenticationServiceImpl");
        }
    }

    private HttpServletRequest getAsHttpRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }
        return (HttpServletRequest) request;
    }

    private String extractAuthTokenFromRequest(HttpServletRequest httpRequest) {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        /* If token not found get it from request parameter */
        if (authToken == null) {
            authToken = httpRequest.getParameter("token");
        }

        return authToken;
    }
}
