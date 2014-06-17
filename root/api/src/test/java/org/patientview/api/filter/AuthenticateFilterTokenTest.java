package org.patientview.api.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Matchers.eq;
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
    private AuthenticateTokenFilter authenticateTokenFilter = new AuthenticateTokenFilter();

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
    public void testFilterForwardsLoginRequest() {
        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/auth/login");
        MockHttpServletResponse rsp = new MockHttpServletResponse();

        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test: To see if the filter will collect the token and authenticate
     * Fail: The filter will try and authenticate the request
     *
     */
    @Test
    public void testAuthenticationFromToken() {


        //Create a token
        String token = "token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        User user = new User();
        userToken.setUser(user);

        when(authenticationService.getToken(eq(token))).thenReturn(userToken);

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/user/get/1");
        MockHttpServletResponse rsp = new MockHttpServletResponse();
        req.addHeader("X-Auth-Token", token);
        try {
            authenticateTokenFilter.doFilter(req, rsp, mockChain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(authenticationService, Mockito.times(1)).getToken(eq(token));

    }

}
