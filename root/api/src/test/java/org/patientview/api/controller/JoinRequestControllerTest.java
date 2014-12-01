package org.patientview.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.JoinRequestService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
public class JoinRequestControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private JoinRequestService joinRequestService;

    @InjectMocks
    private JoinRequestController joinRequestController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(joinRequestController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: The submission of a JoinRequest object to the controller
     * Fail: The JoinRequest object is not passed to the server
     */
    @Test
    public void testAddJoinRequest() {
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);

        Long groupId = 2L;
        joinRequest.setGroupId(groupId);

        try {
            when(joinRequestService.add(eq(joinRequest))).thenReturn(joinRequest);
            mockMvc.perform(MockMvcRequestBuilders.post("/public/joinrequest")
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(joinRequestService, Mockito.times(1)).add(eq(joinRequest));
        } catch (Exception e) {
            fail("This call should not fail");
        }
    }

    @Test
    public void testMigrateJoinRequests() {
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);
        Long groupId = 2L;
        joinRequest.setGroupId(groupId);

        List<JoinRequest> joinRequests = new ArrayList<>();
        joinRequests.add(joinRequest);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/migrate/joinrequests")
                    .content(mapper.writeValueAsString(joinRequests)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(joinRequestService, Mockito.times(1)).migrate(eq(joinRequests));
        } catch (Exception e) {
            fail("This call should not fail");
        }
    }

    /**
     * Test: The request of get all of the join request statuses
     * Fail: The service does not get called for join requests
     */
    @Test
    public void testJoinRequest_statuses() throws ResourceNotFoundException, JsonProcessingException {
        String url = "/joinrequest/statuses";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));

        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testGetByUser() throws ResourceNotFoundException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.getPrincipal();

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/joinrequests"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testGetByUser_withParameterAndData() throws ResourceNotFoundException, JsonProcessingException{
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.getPrincipal();

        String url = "/user/" + user.getId() + "/joinrequests?page=0&size=1&sortDirection=&sortField=";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    /**
     * Test: Saving a join request from the admin screen
     * Fail: The save method is not found
     */
    @Test
    public void testJoinRequestSave() throws ResourceNotFoundException {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);
        joinRequest.setSurname("Test");
        joinRequest.setForename("James");
        joinRequest.setNhsNumber("324234");
        joinRequest.setDateOfBirth(new Date());

        String url = "/joinrequest";

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(joinRequestService, Mockito.times(1)).save(eq(joinRequest));
    }

    /**
     * Test: The request of the count of join request
     */
    @Test
    public void testCountOfJoinRequest() throws ResourceNotFoundException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/joinrequests/count"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }
}
