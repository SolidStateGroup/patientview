package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.SecretWordInput;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class UserControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private UriComponentsBuilder uriComponentsBuilder;

    @Mock
    private UserService userService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AuditAspect auditAspect = AuditAspect.aspectOf();

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController, uriComponentsBuilder).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAddFeature() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to add feature for
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        Feature feature = TestUtils.createFeature("testFeature");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + staffUser.getId() + "/features/" + feature.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test: Adding a GroupRole to a user
     * Fail: The GroupService method does not get called
     */
    @Test
    public void testAddGroupRole() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // user to modify
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        // new role
        Role newStaffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);

        String url = "/user/" + staffUser.getId() + "/group/" + group.getId() + "/role/" + newStaffRole.getId();

        mockMvc.perform(MockMvcRequestBuilders.post(url)).andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, times(1)).addGroupRole(
                eq(staffUser.getId()), eq(group.getId()), eq(newStaffRole.getId()));
    }

    @Test
    public void testAddInformation() throws Exception {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        UserInformation userInformation
                = TestUtils.createUserInformation(user, UserInformationTypes.SHOULD_KNOW, "shouldKnow");
        List<UserInformation> userInformations = new ArrayList<>();
        userInformations.add(userInformation);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/information")
                .content(mapper.writeValueAsString(userInformations)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testChangeSecretWord() throws Exception {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);

        SecretWordInput secretWordInput = new SecretWordInput("abc1234", "abc1234");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + testUser.getId() + "/changeSecretWord")
                .content(mapper.writeValueAsString(secretWordInput)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testCreateUser() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User postUser = TestUtils.createUser("testPost");

        org.patientview.api.model.User apiUser = new org.patientview.api.model.User(user);
        apiUser.setId(1L);

        when(userService.createUserWithPasswordEncryption(eq(postUser))).thenReturn(apiUser.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                .content(mapper.writeValueAsString(postUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testDeleteFeature() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to add feature for
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        Feature feature = TestUtils.createFeature("testFeature");

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + staffUser.getId() + "/features/" + feature.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test: Removing a GroupRole from a user
     * Fail: The GroupService method does not get called
     */
    @Test
    public void testDeleteGroupRole() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // user to modify
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        String url = "/user/" + staffUser.getId() + "/group/" + group.getId() + "/role/" + staffRole.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, times(1)).deleteGroupRole(
                eq(staffUser.getId()), eq(group.getId()), eq(staffRole.getId()));
    }

    @Test
    public void testDeletePicture() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + user.getId() + "/picture"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testDeleteUser() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // user to delete
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        String url = "/user/" + staffUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, times(1)).delete(eq(staffUser.getId()), eq(false));
    }

    @Test
    public void testFindByUsername() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("user2");

        when(userService.getByUsername(eq(user2.getUsername())))
                .thenReturn(new org.patientview.api.model.User(user2));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/username/" + user2.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetInformation() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/information"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test: Send a GET request with a long parameter to the user service
     * Fail: The service does not get called with the parameter
     */
    @Test
    public void testGetUser() throws Exception {
        User user = new User();
        user.setId(1L);

        when(userService.get(eq(user.getId()))).thenReturn(user);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + Long.toString(user.getId())))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testHideSecretWordNotification() throws Exception {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + testUser.getId() + "/hideSecretWordNotification"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testListDuplicateGroupRoles() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/admin/listduplicategrouproles"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, times(1)).listDuplicateGroupRoles();
    }

    @Test
    public void testRemoveSecretWord() throws Exception {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + testUser.getId() + "/secretword"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test: The url to reset a password
     * Fail: The service method does not get called
     * <p>
     * * TODO Fix verify - possible problem with aspect
     */
    @Test
    public void testResetPassword() throws Exception {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser, Collections.EMPTY_LIST);

        String url = "/user/" + testUser.getId() + "/resetPassword";
        Credentials credentials = new Credentials();
        credentials.setPassword("newPassword");
        credentials.setUsername(testUser.getUsername());

        when(auditService.save(any(Audit.class))).thenReturn(new Audit());

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testSendVerificationEmail() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to send verification email
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + staffUser.getId() + "/sendVerificationEmail"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUndelete() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to undelete
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        staffUser.setDeleted(true);
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + staffUser.getId() + "/undelete"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test: The url to change a password
     * Fail: The service method does not get called
     * TODO Fix verify - possible problem with aspect
     */
    @Test
    public void testUpdatePassword() throws Exception {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);

        String url = "/user/" + testUser.getId() + "/changePassword";
        Credentials credentials = new Credentials();
        credentials.setPassword("newPassword");
        credentials.setUsername(testUser.getUsername());

        when(auditService.save(any(Audit.class))).thenReturn(new Audit());

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUpdateUser() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // user to update
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        String url = "/user";

        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .content(mapper.writeValueAsString(staffUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUsernameExists() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User user2 = new User();
        user2.setUsername("test");
        user2.setId(1L);

        when(userService.usernameExists(eq(user2.getUsername()))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/usernameexists/" + user2.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetUserStats() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId()+"/stats"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, times(1)).getUserStats(user.getId());
    }
}
