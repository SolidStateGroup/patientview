package org.patientview.api.controller;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */


public class UserControllerTest {

    @Inject
    private WebApplicationContext  webApplicationContext;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

    }

    /**
     * Test: Send a GET request with a long parameter to the user service
     * Fail: The service does not get called with the parameter
     *
     */
    @Test
    public void testGetUser() {

        Long testUserId = 10L;

        when(userService.getUser(eq(testUserId))).thenReturn(new User());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + Long.toString(testUserId)))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }



    }

}
