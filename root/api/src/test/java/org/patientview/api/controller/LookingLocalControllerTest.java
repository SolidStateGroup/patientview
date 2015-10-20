package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.LookingLocalRoutes;
import org.patientview.api.service.LookingLocalService;
import org.patientview.test.util.TestUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
public class LookingLocalControllerTest {

    private static final String username = "username";
    private static final String password = "password";
    private static final String token = "1234567890";

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private LookingLocalService lookingLocalService;

    @InjectMocks
    private LookingLocalController lookingLocalController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(lookingLocalController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(LookingLocalRoutes.LOOKING_LOCAL_HOME))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(lookingLocalService, Mockito.times(1)).getHomeXml();
    }

    @Test
    public void testAuth() throws Exception {
        when(authenticationService.authenticate(eq(username), eq(password)))
                .thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post(LookingLocalRoutes.LOOKING_LOCAL_AUTH
                + "?username=" + username + "&password=" + password))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticate(eq(username), eq(password));
        verify(lookingLocalService, Mockito.times(1)).getLoginSuccessfulXml(eq(token));
    }

    @Test
    public void testAuth_noUsername() throws Exception {
        String username = "";

        when(authenticationService.authenticate(eq(username), eq(password)))
                .thenThrow(new UsernameNotFoundException("username not found"));

        mockMvc.perform(MockMvcRequestBuilders.post(LookingLocalRoutes.LOOKING_LOCAL_AUTH
                + "?username=" + username + "&password=" + password))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticate(eq(username), eq(password));
        verify(lookingLocalService, Mockito.times(1)).getAuthErrorXml();
    }

    @Test
    public void testAuth_wrongCredentials() throws Exception {
        String password = "wrong_password";

        when(authenticationService.authenticate(eq(username), eq(password)))
                .thenThrow(new AuthenticationServiceException("incorrect password"));

        mockMvc.perform(MockMvcRequestBuilders.post(LookingLocalRoutes.LOOKING_LOCAL_AUTH
                + "?username=" + username + "&password=" + password))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticate(eq(username), eq(password));
        verify(lookingLocalService, Mockito.times(1)).getAuthErrorXml();
    }

    @Test
    public void testMain() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(LookingLocalRoutes.LOOKING_LOCAL_MAIN + "?token=" + token))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(lookingLocalService, Mockito.times(1)).getMainXml(eq(token));
    }
}


