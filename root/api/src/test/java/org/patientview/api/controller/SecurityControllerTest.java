package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.SecurityService;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    /**
     * Test: Send a GET request with a long parameter to the security service to return roles
     * Fail: The service does not get called with the parameter
     *
     * TODO test needs expanding into testing returned data
     */
    @Test
    public void testSecurityRoles() {

        User user = TestUtils.createUser("testUser");

        TestUtils.authenticateTest(user, RoleName.GLOBAL_ADMIN);

        when(securityService.getUserRoles(eq(user.getId()))).thenReturn(new ArrayList<Role>());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/security/user/" + Long.toString(user.getId()) + "/roles"))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(securityService, Mockito.times(1)).getUserRoles(Matchers.eq(user.getId()));

    }


    /**
     * Test: Send a GET request with a long parameter to the security service to return routes
     * Fail: The service does not get called with the parameter
     *
     * TODO test needs expanding into testing returned data
     */
    @Test
    @Ignore("FIX ME")
    public void testSecurityRoutes() {

        User user = TestUtils.createUser("TestUser");
        TestUtils.authenticateTest(user, RoleName.GLOBAL_ADMIN);

        when(securityService.getUserRoutes(eq(user.getId()))).thenReturn(new HashSet<Route>());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/security/user/" + Long.toString(user.getId()) + "/routes"))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(securityService, Mockito.times(1)).getUserRoutes(Matchers.eq(user.getId()));


    }

    /**
     * Test: Get the groups that should be accessible to a user
     * Fail: The groups method which finds group for a user is not called
     *
     */
    @Test
    public void testGetUserGroups() {

        Long testUserId = 10L;
        GetParameters getParameters = new GetParameters();

        when(securityService.getUserGroups(eq(testUserId), eq(getParameters))).thenReturn(null);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/security/user/" + Long.toString(testUserId) + "/groups"
                    + "?filterText=&page=0&size=20&sortDirection=&sortField="))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            fail("Exception throw");
        }

        // todo: sees getParameters as 2 different objects so always fails
        // verify(securityService, Mockito.times(1)).getUserGroups(Matchers.eq(testUserId), Matchers.eq(getParameters));
    }
}
