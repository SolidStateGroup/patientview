package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.model.Email;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import javax.persistence.EntityManager;
import java.util.HashSet;

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
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private UserFeatureRepository userFeatureRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityManager entityManager;

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

    /**
     * Test: The creation of the user with user features, groups and roles
     * Fail: The creation of the user fails without creating groups or user features
     *
     */
    @Test
    public void testCreateUser() {

        User creator = TestUtils.createUser("testCreateUser");
        User newUser = TestUtils.createUser("newTestUser");
        TestUtils.authenticateTest(newUser);
        Feature feature = TestUtils.createFeature("TEST_FEATURE");

        // Add test feature
        UserFeature userFeature = TestUtils.createUserFeature(feature, newUser);
        newUser.setUserFeatures(new HashSet<UserFeature>());
        newUser.getUserFeatures().add(userFeature);

        // Add test role group
        Role role = TestUtils.createRole(RoleName.PATIENT);
        Group group = TestUtils.createGroup("TEST_GROUP");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, newUser);
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.getGroupRoles().add(groupRole);

        // Add test identifier, with lookup type IDENTIFIER, value NHS_NUMBER
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, newUser, "342343424");
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.getIdentifiers().add(identifier);

        when(userRepository.save(Matchers.eq(newUser))).thenReturn(newUser);
        when(groupRepository.findOne(Matchers.eq(group.getId()))).thenReturn(group);
        when(roleRepository.findOne(Matchers.eq(role.getId()))).thenReturn(role);
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRole);

        userService.createUserWithPasswordEncryption(newUser);
    }

    /**
     * Test: To create an identifier on a user record
     * Fail: Identifier does not get created
     */
    @Test
    public void testAddIdentifier() throws ResourceNotFoundException {
        Long userId = 1L;
        User user = TestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setId(3L);

        when(userRepository.findOne(Matchers.eq(userId))).thenReturn(user);
        userService.addIdentifier(userId, identifier);
        verify(identifierRepository, Mockito.times(1)).save(Matchers.eq(identifier));
    }

    /**
     * Test: Get identifier by value
     * Fail: Service is not called
     *
     */
    @Test
    public void testGetIdentifierByValue() throws ResourceNotFoundException {
        String identifierValue = "1111111111";
        Identifier identifier = new Identifier();
        identifier.setIdentifier(identifierValue);
        identifier.setId(1L);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);
        Identifier foundIdentifier = userService.getIdentifierByValue(identifier.getIdentifier());
        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
        Assert.assertTrue("Identifier should be found", foundIdentifier != null);
    }

    /**
     * Test: Password reset check
     * Fail: Service is not called and the change password flag is not set
     *
     */
    @Test
    public void testPasswordReset() throws ResourceNotFoundException {
        String password = "newPassword";
        User user = TestUtils.createUser("testPasswordUser");
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        userService.resetPassword(user.getId(), password);
        verify(userRepository, Mockito.times(1)).findOne(eq(user.getId()));
        Assert.assertTrue("The user now has the change password flag set", user.getChangePassword());
    }

    /**
     * Test: Password change check
     * Fail: Service is not called and the change password is still set.
     *
     */
    @Test
    public void testPasswordChange() throws ResourceNotFoundException {
        String password = "newPassword";
        User user = TestUtils.createUser("testPasswordUser");
        user.setChangePassword(Boolean.TRUE);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        userService.changePassword(user.getId(), password);
        verify(userRepository, Mockito.times(1)).findOne(eq(user.getId()));
        Assert.assertTrue("The user now has the change password flag set", !user.getChangePassword());
    }

    /**
     * Test: Update a user with a new password and set the change flag.
     * Fail: Does not find the Resource
     * @throws ResourceNotFoundException
     */
    @Test
    public void testGetUserByUsernameAndPassword() throws ResourceNotFoundException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(user);
        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail());

        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(1)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: Update a user with a new password but the user's email is wrong
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetUserByUsernameAndPassword_WrongEmail() throws ResourceNotFoundException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(user);

        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail");

        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: Update a user with a new password but the username does not exist
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetUserByUsernameAndPassword_WrongUsername() throws ResourceNotFoundException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(null);

        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail");

        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
        verify(userRepository, Mockito.times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }


    /**
     * Test: To save a Group with Role to a user
     * Fail: The repository does not get called
     *
     * Matching is required on the save call
     */
    @Test
    public void testAddGroupRole() {
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Role testRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(testRole, testGroup, testUser);

        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(roleRepository.findOne(Matchers.eq(testRole.getId()))).thenReturn(testRole);
        when(groupRoleRepository.save(Matchers.any(GroupRole.class))).thenReturn(groupRole);

        groupRole = userService.addGroupRole(testUser.getId(), testGroup.getId(), testRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, Mockito.times(1)).save(Matchers.any(GroupRole.class));
    }
}
