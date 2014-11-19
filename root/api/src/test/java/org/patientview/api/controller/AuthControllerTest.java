package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.ForgottenCredentials;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.MessagingException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class AuthControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

    }

    /**
     * Test: Call the /auth/login and pass the username and password as json
     * Fail: The username and password are not passed into the service layer.
     *
     */
    @Test
    public void testAuthenticate() {

        Credentials credentials = new Credentials();
        credentials.setUsername("testUser");
        credentials.setPassword("doNotShow");

        try {
            when(authenticationService.authenticate(eq(credentials.getUsername()),
                    eq(credentials.getPassword()))).thenReturn("");
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(authenticationService, Mockito.times(1)).authenticate(eq(credentials.getUsername()),
                    eq(credentials.getPassword()));
        } catch (Exception e) {
            fail("Exception throw");
        }

    }


    /**
     * Test: The url for resetting a password from a Username and Email
     * Fail: The service method is not called
     *
     */
    @Test
    public void testForgottenPassword() throws ResourceNotFoundException, MailException, MessagingException {

        ForgottenCredentials forgottenCredentials = new ForgottenCredentials();
        forgottenCredentials.setEmail("rememberedEmail");
        forgottenCredentials.setUsername("rememberedUsername");

        String url = "/auth/forgottenpassword";

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .content(mapper.writeValueAsString(forgottenCredentials)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("The post request should not fail " + e.getCause());
        }

        verify(userService, Mockito.times(1))
                .resetPasswordByUsernameAndEmail(
                        Matchers.eq(forgottenCredentials.getUsername()), Matchers.eq(forgottenCredentials.getEmail()));

    }


}
