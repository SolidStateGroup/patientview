package org.patientview.api.controller;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.SecurityService;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test associated with the user security
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public class SecurityControllerTest extends BaseControllerTest<SecurityController> {


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


    /**
     * Test: Send a GET request with a long parameter to the security service to return roles
     * Fail: The service does not get called with the parameter
     *
     * TODO test needs expanding into testing returned data
     */
    @Test
    public void testSecurityRoles() {

        Long testUserId = 10L;

        when(securityService.getUserRoles(eq(testUserId))).thenReturn(new ArrayList<Role>());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/security/user/" + Long.toString(testUserId) + "/roles"))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(securityService, Mockito.times(1)).getUserRoles(Matchers.eq(testUserId));

    }


    /**
     * Test: Send a GET request with a long parameter to the security service to return routes
     * Fail: The service does not get called with the parameter
     *
     * TODO test needs expanding into testing returned data
     */
    @Test
    public void testSecurityRoutes() {

        Long testUserId = 10L;

        when(securityService.getUserRoutes(eq(testUserId))).thenReturn(new ArrayList<Route>());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/security/user/" + Long.toString(testUserId) + "/routes"))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(securityService, Mockito.times(1)).getUserRoutes(Matchers.eq(testUserId));


    }

}
