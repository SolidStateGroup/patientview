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

/**
 * Class to get the filter from the request. Lookup the token and add the user into the security context
 *
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@WebFilter(urlPatterns = {"*"}, filterName = "authenticationTokenFilter")
public class AuthenticateTokenFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateTokenFilter.class);

    private AuthenticationService authenticationService;

    @PostConstruct
    public void init() {
        LOG.info("Authentication token filter initialised");
    }


    /**
     * This is the method to authorize the user to use the service is spring.
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {


        HttpServletRequest httpRequest = this.getAsHttpRequest(request);

        // TODO Fix for Spring Boot bug with using delegating proxy
        setAuthenticationManager(request);

        String path = httpRequest.getRequestURI();

        if (path.contains("/error")) {
            chain.doFilter(request, response);

        } else {
            // Fix for CORS not required for PROD
            if (httpRequest.getMethod().equalsIgnoreCase("options")) {
                chain.doFilter(request, response);
            } else if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/logout")) {
                chain.doFilter(request, response);
            } else {
                if (!authenticateRequest(httpRequest)) {
                    redirectFailedAuthentication((HttpServletResponse) response);
                }
                chain.doFilter(request, response);
            }
        }

    }

    // Set the authentication in the security context
    private boolean authenticateRequest(HttpServletRequest httpServletRequest) {
        LOG.debug("Filtering on path {}", httpServletRequest.getRequestURL().toString());

        String authToken = this.extractAuthTokenFromRequest(httpServletRequest);
        PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(authToken, authToken);

        try {
            Authentication authentication = authenticationService.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (AuthenticationServiceException e) {
            LOG.info("Authentication failed for {}", authenticationToken.getName());
            return false;
        }

    }

    private void redirectFailedAuthentication(HttpServletResponse response) {
        try {
            response.sendRedirect("/api/error");
        } catch(IOException ioe) {
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
