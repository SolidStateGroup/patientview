package org.patientview.api.filter;

import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Class to get the filter from the request. Lookup the token and add the user into the security context
 *
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@WebFilter(urlPatterns = {"/test*"})
public class AuthenticateTokenFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateTokenFilter.class);

    @Inject
    @Qualifier(value = "authenticationService")
    private AuthenticationService authenticationService;

    @Inject
    ApplicationContext applicationContext;

    @PostConstruct
    public void init() {

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


        String path = ((HttpServletRequest) request).getRequestURI();
        if (path.contains("/auth/login")) {
            chain.doFilter(request, response); // Just continue chain for login
        } else {

            LOG.debug("Filtering on path {}", ((HttpServletRequest) request).getRequestURL().toString());

            HttpServletRequest httpRequest = this.getAsHttpRequest(request);
            String authToken = this.extractAuthTokenFromRequest(httpRequest);
            UserToken userToken = authenticationService.getToken(authToken);

            if (userToken != null) {

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userToken.getUser(), null, userToken.getUser().getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {

            }

            chain.doFilter(request, response);
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
