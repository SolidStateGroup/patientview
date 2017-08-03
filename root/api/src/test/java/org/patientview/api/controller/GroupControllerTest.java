package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.service.GroupService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private SecurityAspect securityAspect = SecurityAspect.aspectOf();

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetGroupsPublic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/public/group"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(groupService, Mockito.times(1)).findAllPublic();
    }

    @Test
    public void testAddChildGroup() throws Exception {
        Long groupId = 1L;
        Long childGroupId = 2L;
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.put("/group/" + groupId + "/child/" + childGroupId))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(groupService, Mockito.times(1)).addChildGroup(eq(groupId), eq(childGroupId));
    }

    @Test
    public void testAddParentGroup() throws Exception {
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

        mockMvc.perform(MockMvcRequestBuilders.put("/group/" + group.getId() + "/parent/" + parentGroup.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(groupService, Mockito.times(1)).addParentGroup(eq(group.getId()), eq(parentGroup.getId()));
    }

    @Test
    public void testAddFeature() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.put("/group/" + group.getId() + "/features/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testDeleteFeature() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        String url = "/group/" + group.getId() + "/features/1";

        mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
