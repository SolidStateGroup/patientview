package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.controller.model.Credentials;
import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.UserToken;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
                    eq(credentials.getPassword()))).thenReturn(new UserToken());
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(authenticationService, Mockito.times(1)).authenticate(eq(credentials.getUsername()),
                    eq(credentials.getPassword()));
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

    }


}
