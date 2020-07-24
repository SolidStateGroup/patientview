package org.patientview.api.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 17/06/2014
 *
 * http://stackoverflow.com/questions/11451917/how-do-i-unit-test-a-serlvet-filter-with-junit
 */
public class AuthenticateFilterTokenTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticateTokenFilter authenticateTokenFilter = new AuthenticateTokenFilter(authenticationService);

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test: To see if the filter will bypass requests to the /auth/filter page
     * Fail: The filter will try and authenticate the request
     *
     */
    @Test
    public void testFilterForwardsLoginRequest() throws Exception {
        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/auth/login");
        MockHttpServletResponse rsp = new MockHttpServletResponse();

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        when(authenticationService.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, userToken, new ArrayList<GroupRole>()));

        authenticateTokenFilter.doFilter(req, rsp, mockChain);
    }

    /**
     * Test: To see if the filter will collect the token and authenticate
     * Fail: The filter will try and authenticate the request
     *
     */
    @Test
    public void testAuthenticationFromToken_normalRequest() throws Exception {

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/api/user/get/1");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);

        when(authenticationService.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, userToken, new ArrayList<GroupRole>()));

        authenticateTokenFilter.doFilter(req, rsp, mockChain);

        verify(authenticationService, Mockito.times(1)).authenticate(any(Authentication.class));
    }

    /**
     * Test: To see if the filter will avoid authenticating /api/auth/login
     * Fail: The filter will not authenticate the request
     *
     */
    @Test
    public void testAuthenticationFromToken_loginRequest() {

        // required during testing
        authenticateTokenFilter.init();

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/api/auth/login");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);
        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(authenticationService, Mockito.times(0)).authenticate(any(Authentication.class));
    }

    /**
     * Test: To see if the filter will avoid authenticating /api/auth/logout
     * Fail: The filter will not authenticate the request
     *
     */
    @Test
    public void testAuthenticationFromToken_logoutRequest() {

        // required during testing
        authenticateTokenFilter.init();

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/api/auth/logout");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);
        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(authenticationService, Mockito.times(0)).authenticate(any(Authentication.class));
    }

    /**
     * Test: To see if the filter will avoid authenticating /api/error
     * Fail: The filter will not authenticate the request
     *
     */
    @Test
    public void testAuthenticationFromToken_errorRequest() {

        // required during testing
        authenticateTokenFilter.init();

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/api/error");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);
        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(authenticationService, Mockito.times(0)).authenticate(any(Authentication.class));

    }

    /**
     * Test: To see if the filter will redirect unauthorised login
     * Fail: The filter will not redirect the request to the error page
     *
     */
    @Test
    @Ignore("Disabling forwarding for now")
    public void testAuthenticationFromToken_invalidRequest() {

        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/api/user/1");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);

        when(authenticationService.authenticate(any(Authentication.class))).thenThrow(AuthenticationServiceException.class);
        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(authenticationService, Mockito.times(1)).authenticate(any(Authentication.class));
        Assert.assertTrue("The request should have been forwarded to '/api/error'", rsp.getRedirectedUrl().equals("/api/error"));
    }


}
