package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.persistence.model.Email;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserInformationRepository;
import org.patientview.persistence.repository.UserMigrationRepository;
import org.patientview.persistence.repository.UserObservationHeadingRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
public class UserServiceTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private UserFeatureRepository userFeatureRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private UserInformationRepository userInformationRepository;

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private UserMigrationRepository userMigrationRepository;

    @Mock
    private UserObservationHeadingRepository userObservationHeadingRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Properties properties;

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Mock
    AuditService auditService;

    @InjectMocks
    AuditAspect auditAspect = AuditAspect.aspectOf();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateUser() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User newUser = TestUtils.createUser("newTestUser");
        Feature feature = TestUtils.createFeature("TEST_FEATURE");

        // Add test feature
        UserFeature userFeature = TestUtils.createUserFeature(feature, newUser);
        newUser.setUserFeatures(new HashSet<UserFeature>());
        newUser.getUserFeatures().add(userFeature);

        // Add test role group
        Role role2 = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, group, newUser);
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.getGroupRoles().add(groupRole2);

        // Add test identifier, with lookup type IDENTIFIER, value NHS_NUMBER
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, newUser, "342343424");
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.getIdentifiers().add(identifier);

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.getOne(any(Long.class))).thenReturn(newUser);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(groupRepository.exists(eq(group.getId()))).thenReturn(true);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(roleRepository.findOne(eq(role2.getId()))).thenReturn(role2);
        when(groupRoleRepository.userGroupRoleExists(any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(false);
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(groupRole2);

        userService.createUserWithPasswordEncryption(newUser);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCreateUserWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User newUser = TestUtils.createUser("newTestUser");
        Feature feature = TestUtils.createFeature("TEST_FEATURE");

        // Add test feature
        UserFeature userFeature = TestUtils.createUserFeature(feature, newUser);
        newUser.setUserFeatures(new HashSet<UserFeature>());
        newUser.getUserFeatures().add(userFeature);

        // Add test role group
        Role role2 = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, group, newUser);
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.getGroupRoles().add(groupRole2);

        // Add test identifier, with lookup type IDENTIFIER, value NHS_NUMBER
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, newUser, "342343424");
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.getIdentifiers().add(identifier);

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.getOne(any(Long.class))).thenReturn(newUser);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(groupRepository.exists(eq(group.getId()))).thenReturn(true);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRole);

        userService.createUserWithPasswordEncryption(newUser);
    }

    @Test
    public void testUpdateUser() throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to save
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(groupRepository.exists(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(roleRepository.findOne(eq(staffRole.getId()))).thenReturn(staffRole);

        userService.save(staffUser);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testUpdateUserWrongGroup()
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role role = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to save
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(groupRepository.exists(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

        userService.save(staffUser);
    }

    /**
     * Test: Password reset check
     * Fail: Service is not called and the change password flag is not set
     */
    @Test
    public void testPasswordReset() throws ResourceNotFoundException, ResourceForbiddenException, MessagingException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to reset password for
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        String password = "newPassword";
        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(userRepository.save(eq(staffUser))).thenReturn(staffUser);

        org.patientview.api.model.User user1 = userService.resetPassword(staffUser.getId(), password);

        verify(userRepository, Mockito.times(2)).findOne(eq(staffUser.getId()));
        verify(userRepository, Mockito.times(1)).save(eq(staffUser));
        Assert.assertTrue("The user now has the change password flag set", user1.getChangePassword());
    }

    /**
     * Test: Password change check
     * Fail: Service is not called
     */
    @Test
    public void testPasswordChange() throws ResourceNotFoundException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String password = "newPassword";

        user.setChangePassword(Boolean.TRUE);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        userService.changePassword(user.getId(), password);
        verify(userRepository, Mockito.times(3)).findOne(eq(user.getId()));
    }

    /**
     * Test: User has forgotten password. Update a user with a new password and set the change flag.
     * Fail: Does not find the Resource
     * @throws ResourceNotFoundException
     */
    @Test
    public void testResetPassword() throws ResourceNotFoundException, MailException, MessagingException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(user);
        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail());

        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(1)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: User has forgotten password. Update a user with a new password but the user's email is wrong
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testResetPassword_WrongEmail() throws ResourceNotFoundException, MailException, MessagingException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(user);

        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail");

        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: User has forgotten password. Update a user with a new password but the username does not exist
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testResetPassword_WrongUsername() throws ResourceNotFoundException, MailException, MessagingException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(null);

        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail");

        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: To save a Group with Role to a user
     * Fail: The repository does not get called
     */
    @Test
    public void testAddGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to modify
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        // new role
        Role newStaffRole = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(groupRepository.findOne(eq(group2.getId()))).thenReturn(group2);
        when(roleRepository.findOne(eq(newStaffRole.getId()))).thenReturn(newStaffRole);
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(groupRole);

        // add GroupRole to staff user
        groupRole = userService.addGroupRole(staffUser.getId(), group.getId(), newStaffRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, Mockito.times(1)).save(any(GroupRole.class));
    }

    /**
     * Test: To remove a group role
     * Fail: The repository does not get called
     */
    @Test
    public void testDeleteGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to modify
        Group group2 = TestUtils.createGroup("testGroup2");
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(groupRepository.findOne(eq(group2.getId()))).thenReturn(group2);
        when(roleRepository.findOne(eq(staffRole.getId()))).thenReturn(staffRole);
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRoleStaff);

        // add GroupRole to staff user
        userService.deleteGroupRole(staffUser.getId(), group.getId(), staffRole.getId());
        verify(groupRoleRepository, Mockito.times(1)).delete(any(GroupRole.class));
    }

    @Test
    public void testDeleteUser_patient() throws ResourceNotFoundException, ResourceForbiddenException,
            FhirResourceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to delete
        User staffUser = TestUtils.createUser("patient");
        Role staffRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(roleRepository.findOne(eq(staffRole.getId()))).thenReturn(staffRole);

        userService.delete(staffUser.getId(), false);
        verify(userRepository, Mockito.times(1)).delete(any(User.class));
    }

    @Test
    public void testDeleteUser_staff() throws ResourceNotFoundException, ResourceForbiddenException,
            FhirResourceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to delete
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(roleRepository.findOne(eq(role.getId()))).thenReturn(role);
        when(roleRepository.findOne(eq(staffRole.getId()))).thenReturn(staffRole);

        userService.delete(staffUser.getId(), false);
        verify(userRepository, Mockito.times(0)).delete(any(User.class));
    }

    @Test
    public void testSendVerificationEmail()
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to send verification email
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(properties.getProperty((eq("smtp.sender")))).thenReturn("test@solidstategroup.com");

        userService.sendVerificationEmail(staffUser.getId());
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void testAddFeature() throws ResourceNotFoundException, ResourceForbiddenException {

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

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(featureRepository.findOne(eq(feature.getId()))).thenReturn(feature);

        userService.addFeature(staffUser.getId(), feature.getId());
        verify(userFeatureRepository, Mockito.times(1)).save(any(UserFeature.class));
    }

    @Test
    public void testDeleteFeature() throws ResourceNotFoundException, ResourceForbiddenException {

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
        UserFeature userFeature = TestUtils.createUserFeature(feature, staffUser);

        when(userRepository.findOne(eq(staffUser.getId()))).thenReturn(staffUser);
        when(featureRepository.findOne(eq(feature.getId()))).thenReturn(feature);
        when(userFeatureRepository.findByUserAndFeature(user, feature)).thenReturn(userFeature);

        userService.deleteFeature(staffUser.getId(), feature.getId());
        verify(userFeatureRepository, Mockito.times(1)).delete(any(UserFeature.class));
    }

    @Test
    public void testGetInformation() throws ResourceNotFoundException {

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

        when(userInformationRepository.findByUser(eq(user))).thenReturn(userInformations);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);

        userService.getInformation(user.getId());
        verify(userInformationRepository, Mockito.times(1)).findByUser(any(User.class));
    }

    @Test
    public void testAddInformation() throws ResourceNotFoundException {

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

        when(userInformationRepository.save(any(UserInformation.class))).thenReturn(userInformation);
        when(userInformationRepository.findByUserAndType(eq(user), any(UserInformationTypes.class))).thenReturn(userInformation);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);

        userService.addInformation(user.getId(), userInformations);
        verify(userInformationRepository, Mockito.times(1)).save(any(UserInformation.class));
    }

    @Test
    public void testGetByIdentifier() throws ResourceNotFoundException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User user1 = TestUtils.createUser("testForgottenPassword");
        user1.setIdentifiers(new HashSet<Identifier>());

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "342343424");
        user1.setIdentifiers(new HashSet<Identifier>());
        user1.getIdentifiers().add(identifier);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);

        org.patientview.api.model.User user2 = userService.getByIdentifierValue(identifier.getIdentifier());

        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
        Assert.assertNotNull("The user should be returned", user2);
    }

    @Test
    public void testCurrentUserCanSwitchToUser_UnitStaff() {
        Group group1 = TestUtils.createGroup("test1Group");
        Group group2 = TestUtils.createGroup("test2Group");
        Group group3 = TestUtils.createGroup("test3Group");

        // create group relationships
        group3.getGroupRelationships().add(TestUtils.createGroupRelationship(group3, group1, RelationshipTypes.PARENT));

        Role roleUnitStaff = TestUtils.createRole(RoleName.STAFF_ADMIN);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.STAFF);
        roleUnitStaff.setRoleType(roleType);

        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        org.patientview.persistence.model.RoleType roleType2 = new org.patientview.persistence.model.RoleType();
        roleType2.setValue(RoleType.PATIENT);
        rolePatient.setRoleType(roleType2);

        // current user and security
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(roleUnitStaff, group1, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // patient in same group (yes)
        User switchUser1 = TestUtils.createUser("switch1");
        switchUser1.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group1, switchUser1));

        // patient in another group (no)
        User switchUser2 = TestUtils.createUser("switch2");
        switchUser2.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group2, switchUser2));

        // patient in another group in same specialty (no)
        User switchUser3 = TestUtils.createUser("switch3");
        switchUser3.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group3, switchUser3));

        // unit admin in same group (no)
        User switchUser4 = TestUtils.createUser("switch4");
        switchUser4.getGroupRoles().add(TestUtils.createGroupRole(roleUnitStaff, group1, switchUser4));

        Assert.assertEquals("Should be able to get patient in same unit", true,
                userService.currentUserCanSwitchToUser(switchUser1));
        Assert.assertEquals("Should not be able to get patient in another unit", false,
                userService.currentUserCanSwitchToUser(switchUser2));
        Assert.assertEquals("Should not be able to get patient in same specialty", false,
                userService.currentUserCanSwitchToUser(switchUser3));
        Assert.assertEquals("Should not be able to get unit admin in same unit", false,
                userService.currentUserCanSwitchToUser(switchUser4));
    }

    @Test
    public void testCurrentUserCanSwitchToUser_UnitAdmin() {
        Group group1 = TestUtils.createGroup("test1Group");
        Group group2 = TestUtils.createGroup("test2Group");
        Group group3 = TestUtils.createGroup("test3Group");

        // create group relationships
        group3.getGroupRelationships().add(TestUtils.createGroupRelationship(group3, group1, RelationshipTypes.PARENT));

        Role roleUnitAdmin = TestUtils.createRole(RoleName.UNIT_ADMIN);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.STAFF);
        roleUnitAdmin.setRoleType(roleType);

        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        org.patientview.persistence.model.RoleType roleType2 = new org.patientview.persistence.model.RoleType();
        roleType2.setValue(RoleType.PATIENT);
        rolePatient.setRoleType(roleType2);

        // current user and security
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(roleUnitAdmin, group1, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // patient in same group (yes)
        User switchUser1 = TestUtils.createUser("switch1");
        switchUser1.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group1, switchUser1));

        // patient in another group (no)
        User switchUser2 = TestUtils.createUser("switch2");
        switchUser2.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group2, switchUser2));

        // patient in another group in same specialty (no)
        User switchUser3 = TestUtils.createUser("switch3");
        switchUser3.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group3, switchUser3));

        // unit admin in same group (no)
        User switchUser4 = TestUtils.createUser("switch4");
        switchUser4.getGroupRoles().add(TestUtils.createGroupRole(roleUnitAdmin, group1, switchUser4));

        Assert.assertEquals("Should be able to get patient in same unit", true,
                userService.currentUserCanSwitchToUser(switchUser1));
        Assert.assertEquals("Should not be able to get patient in another unit", false,
                userService.currentUserCanSwitchToUser(switchUser2));
        Assert.assertEquals("Should not be able to get patient in same specialty", false,
                userService.currentUserCanSwitchToUser(switchUser3));
        Assert.assertEquals("Should not be able to get unit admin in same unit", false,
                userService.currentUserCanSwitchToUser(switchUser4));
    }

    @Test
    public void testCurrentUserCanSwitchToUser_SpecialtyAdmin() {
        Group group1 = TestUtils.createGroup("test1Group");
        Group group2 = TestUtils.createGroup("test2Group");
        Group group3 = TestUtils.createGroup("test3Group");

        // create group relationships
        group3.getGroupRelationships().add(TestUtils.createGroupRelationship(group3, group1, RelationshipTypes.PARENT));

        Role roleSpecialtyAdmin = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.STAFF);
        roleSpecialtyAdmin.setRoleType(roleType);

        Role roleUnitAdmin = TestUtils.createRole(RoleName.UNIT_ADMIN);
        roleUnitAdmin.setRoleType(roleType);

        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        org.patientview.persistence.model.RoleType roleType2 = new org.patientview.persistence.model.RoleType();
        roleType2.setValue(RoleType.PATIENT);
        rolePatient.setRoleType(roleType2);

        // current user and security
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(roleSpecialtyAdmin, group1, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // patient in same group (yes)
        User switchUser1 = TestUtils.createUser("switch1");
        switchUser1.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group1, switchUser1));

        // patient in another group (no)
        User switchUser2 = TestUtils.createUser("switch2");
        switchUser2.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group2, switchUser2));

        // patient in another group in same specialty (yes)
        User switchUser3 = TestUtils.createUser("switch3");
        switchUser3.getGroupRoles().add(TestUtils.createGroupRole(rolePatient, group3, switchUser3));

        // unit admin in same group (no)
        User switchUser4 = TestUtils.createUser("switch4");
        switchUser4.getGroupRoles().add(TestUtils.createGroupRole(roleUnitAdmin, group1, switchUser4));

        Assert.assertEquals("Should be able to get patient in same unit", true,
                userService.currentUserCanSwitchToUser(switchUser1));
        Assert.assertEquals("Should not be able to get patient in another unit", false,
                userService.currentUserCanSwitchToUser(switchUser2));
        Assert.assertEquals("Should be able to get patient in same specialty", true,
                userService.currentUserCanSwitchToUser(switchUser3));
        Assert.assertEquals("Should not be able to get unit admin in same unit", false,
                userService.currentUserCanSwitchToUser(switchUser4));
    }

    @Test
    public void testUsernameExists() {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User staffUser = TestUtils.createUser("existing");
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findByUsernameCaseInsensitive(eq(staffUser.getName()))).thenReturn(staffUser);

        userService.usernameExists(staffUser.getUsername());
        verify(userRepository, Mockito.times(1)).findByUsernameCaseInsensitive(eq(staffUser.getUsername()));
    }
}
