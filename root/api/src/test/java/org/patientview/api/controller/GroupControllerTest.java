package org.patientview.api.controller;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.GroupService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private GroupService groupService;

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


}
