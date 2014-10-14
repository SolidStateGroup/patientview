package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.SecurityService;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test associated with the user security
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public class SecurityControllerTest {

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private SecurityController securityController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(securityController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void doNothing() {

    }
}
