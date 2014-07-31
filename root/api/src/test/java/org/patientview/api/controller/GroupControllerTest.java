package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupControllerTest {


    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private AdminService adminService;

    @Mock
    private GroupService groupService;

    @Mock
    private JoinRequestService joinRequestService;

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    public void testGetGroupByType() {

        Long typeId = 9L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/type/" + typeId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).findGroupByType(eq(typeId));

    }

    @Test
    public void testAddParentGroup() {

        Long groupId = 1L;
        Long parentGroupId = 2L;

        String url = "/group/" + groupId + "/parent/" + parentGroupId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addParentGroup(eq(groupId), eq(parentGroupId));
    }

    @Test
    public void testAddFeature() {

        Long groupId = 1L;
        Long featureId = 2L;

        String url = "/group/" + groupId + "/features/" + featureId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addFeature(eq(groupId), eq(featureId));
    }

    @Test
    public void testDeleteFeature() {

        Long groupId = 1L;
        Long featureId = 2L;

        String url = "/group/" + groupId + "/features/" + featureId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).deleteFeature(eq(groupId), eq(featureId));
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

        Long groupId = 2L;

        try {
            when(joinRequestService.addJoinRequest(eq(groupId), eq(joinRequest))).thenReturn(joinRequest);
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/joinRequest")
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(joinRequestService, Mockito.times(1)).addJoinRequest(eq(groupId), eq(joinRequest));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }

    }

    /**
     * Test: The request of a parent group with the child
     * Fail: The 200 is not returned and the service is not called
     */
    @Test
    public void testGetChildGroups() throws ResourceNotFoundException {

        Long groupId = 9L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/" + groupId + "/children")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).findChildren(eq(groupId));

    }

}
