package org.patientview.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.RequestService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RequestStatus;
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
public class RequestControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: The submission of a Request object to the controller
     * Fail: The Request object is not passed to the server
     */
    @Test
    public void testAddRequest() {
        Request request = new Request();
        request.setId(1L);
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setStatus(RequestStatus.SUBMITTED);

        Long groupId = 2L;
        request.setGroupId(groupId);

        try {
            when(requestService.add(eq(request))).thenReturn(request);
            mockMvc.perform(MockMvcRequestBuilders.post("/public/request")
                    .content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(requestService, Mockito.times(1)).add(eq(request));
        } catch (Exception e) {
            fail("This call should not fail");
        }
    }

    /**
     * Test: The request of get all of the request statuses
     * Fail: The service does not get called for requests
     */
    @Test
    public void testRequest_statuses() throws ResourceNotFoundException, JsonProcessingException {
        String url = "/request/statuses";

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
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/requests"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testGetByUser_withParameterAndData() throws ResourceNotFoundException, JsonProcessingException{
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.getPrincipal();

        String url = "/user/" + user.getId() + "/requests?page=0&size=1&sortDirection=&sortField=";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    /**
     * Test: Saving a request from the admin screen
     * Fail: The save method is not found
     */
    @Test
    public void testRequestSave() throws ResourceNotFoundException {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.SUBMITTED);
        request.setSurname("Test");
        request.setForename("James");
        request.setNhsNumber("324234");
        request.setDateOfBirth(new Date());

        String url = "/request";

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                    .content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(requestService, Mockito.times(1)).save(eq(request));
    }

    /**
     * Test: The request of the count of request
     */
    @Test
    public void testCountOfRequest() throws ResourceNotFoundException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/requests/count"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }
}
