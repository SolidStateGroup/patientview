package org.patientview.api.controller;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AdminService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */


public class TestUserController {

    @Inject
    private WebApplicationContext  webApplicationContext;


    @Mock
    private AdminService adminService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

    }

    @Test
    public void testGetUser() {

        Long testUserId = 10L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/{}", testUserId));
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

    }

}
