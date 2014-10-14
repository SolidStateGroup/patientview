package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.model.UnitRequest;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.JoinRequestService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    User creator;

    @Mock
    private GroupService groupService;

    @Mock
    private JoinRequestService joinRequestService;

    @Mock
    private GroupStatisticService groupStatisticService;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private SecurityAspect securityAspect = SecurityAspect.aspectOf();

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        creator = TestUtils.createUser("creator");
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    public void testGetGroupsPublic() {
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/public/group"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).findAllPublic();
    }

    @Test
    public void testAddChildGroup() {
        Long groupId = 1L;
        Long childGroupId = 2L;
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        String url = "/group/" + groupId + "/child/" + childGroupId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addChildGroup(eq(groupId), eq(childGroupId));
    }

    @Test
    public void testAddParentGroup() {

        // groups
        Group group = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("testParentGroup");

        // user and security
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, parentGroup, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        String url = "/group/" + group.getId() + "/parent/" + parentGroup.getId();

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addParentGroup(eq(group.getId()), eq(parentGroup.getId()));
    }

    @Test
    public void testAddFeature() {

        Long groupId = 1L;
        Long featureId = 2L;

        String url = "/group/" + groupId + "/features/" + featureId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
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
            fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).deleteFeature(eq(groupId), eq(featureId));
    }

    @Test
    public void testAddAdditionalLocation() {
        Location location = new Location();
        location.setId(1L);
        location.setLabel("Additional Location");
        location.setName("New location");
        location.setPhone("0123456789");
        location.setAddress("1 Road Street, Town, AB12CD");
        location.setWeb("http://www.additional.com");
        location.setEmail("test@solidstategroup.com");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + group.getId() + "/locations")
                    .content(mapper.writeValueAsString(location)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }


    /**
     * Test: The retrieval of the group statistics for a specific group
     * Fail: The statistics service is not contacted about the request
     */
    @Test
    public void testGroupStatistics() throws ResourceNotFoundException {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/" + group.getId() + "/statistics"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    /**
     * Test: The submission of a password reset object to the controller
     * Fail: The password reset object is not passed to the service
     */
    @Test
    public void testPasswordRequest() {

        UnitRequest unitRequest = new UnitRequest();
        unitRequest.setForename("Test");
        unitRequest.setSurname("User");
        unitRequest.setDateOfBirth(new Date());
        unitRequest.setNhsNumber("ASDASDA");
        Long groupId = 2L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/public/passwordrequest/group/" + groupId)
                    .content(mapper.writeValueAsString(unitRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(groupService, Mockito.times(1)).passwordRequest(eq(groupId), any(UnitRequest.class));
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }
}


