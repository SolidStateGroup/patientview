package org.patientview.api.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.job.DeletePatientTask;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.SecretWordInput;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.Email;
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
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.ApiKeyRepository;
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
import org.patientview.service.AuditService;
import org.patientview.test.util.TestUtils;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
public class UserServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AuditAspect auditAspect = AuditAspect.aspectOf();

    @Mock
    private AuditService auditService;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ExternalServiceService externalServiceService;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private Properties properties;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserFeatureRepository userFeatureRepository;

    @Mock
    private UserInformationRepository userInformationRepository;

    @Mock
    private UserMigrationRepository userMigrationRepository;

    @Mock
    private UserObservationHeadingRepository userObservationHeadingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private DocumentService documentService;

    @Mock
    private ApiMedicationService apiMedicationService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private DeletePatientTask deletePatientTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAdd_staff() throws EntityExistsException, ResourceForbiddenException,
            ResourceNotFoundException, FhirResourceException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // User to add
        User newUser = new User();
        GroupRole newGroupRole = TestUtils.createGroupRole(role, group, newUser);
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.getGroupRoles().add(newGroupRole);
        UserFeature userFeature = TestUtils.createUserFeature(TestUtils.createFeature("feature"), newUser);
        newUser.setUserFeatures(new HashSet<UserFeature>());
        newUser.getUserFeatures().add(userFeature);

        //when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRoleRepository.save(eq(newGroupRole))).thenReturn(newGroupRole);
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(apiKeyRepository.getAllKeysForUser(any(User.class))).thenReturn(new ArrayList<ApiKey>());
        when(userRepository.save(eq(newUser))).thenReturn(newUser);

        userService.add(newUser);

        verify(userFeatureRepository, times(1)).save(eq(userFeature));
        verify(userRepository, times(1)).save(eq(newUser));
        verify(auditService, times(1)).createAudit(eq(AuditActions.ADMIN_ADD), eq(newUser.getUsername()),
                any(User.class), any(Long.class), eq(AuditObjectTypes.User), eq((Group) null));
        verify(auditService, times(1)).createAudit(eq(AuditActions.ADMIN_GROUP_ROLE_ADD), eq(newUser.getUsername()),
                any(User.class), any(Long.class), eq(AuditObjectTypes.User), eq(group));
    }

    @Test
    public void testAddFeature() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(featureRepository.findById(eq(feature.getId()))).thenReturn(Optional.of(feature));

        userService.addFeature(staffUser.getId(), feature.getId());
        verify(userFeatureRepository, times(1)).save(any(UserFeature.class));
    }

    /**
     * Test: To save a Group with Role to a user
     * Fail: The repository does not get called
     */
    @Test
    public void testAddGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group2, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        // new role
        Role newStaffRole = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(newStaffRole.getId()))).thenReturn(Optional.of(newStaffRole));
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(groupRole);

        // add GroupRole to staff user
        groupRole = userService.addGroupRole(staffUser.getId(), group.getId(), newStaffRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, times(1)).save(any(GroupRole.class));

        // verify not queued to RDC, as staff
        verify(externalServiceService, times(0))
                .addToQueue(eq(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION),
                        any(String.class), any(User.class), any(Date.class), any(GroupRole.class));
    }

    /**
     * Test: To save a Group with Role to a user
     * Fail: The repository does not get called
     */
    @Test
    public void testAddGroupRolePatient() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        //        Lookup lookupValue = new Lookup();
        //        lookupValue.setValue("");
        //        group2.setGroupType(lookupValue);
        //        group.setGroupType(lookupValue);


        User patientUser = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(patientRole, group2, patientUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        patientUser.setGroupRoles(groupRolesStaff);

        // new role
        Role newPatientRole = TestUtils.createRole(RoleName.PATIENT);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.PATIENT);
        newPatientRole.setRoleType(roleType);

        when(userRepository.findById(eq(patientUser.getId()))).thenReturn(Optional.of(patientUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(newPatientRole.getId()))).thenReturn(Optional.of(newPatientRole));
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(groupRole);

        // add GroupRole to staff user
        groupRole = userService.addGroupRole(patientUser.getId(), group.getId(), newPatientRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, times(1)).save(any(GroupRole.class));
    }

    /**
     * Test: To save a Group with Role to a user
     * Fail: The repository does not get called
     */
    @Test
    public void testAddGroupRolePatient_With_NHS_number() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
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
        group2.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        Group parent = TestUtils.createGroup("Renal");
        parent.setCode("Renal");
        // create group relationships
        group2.getGroupRelationships().add(TestUtils.createGroupRelationship(group2, parent, RelationshipTypes.PARENT));

        User patientUser = TestUtils.createUser("test_patient");
        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole patientGroupRole = TestUtils.createGroupRole(patientRole, group2, patientUser);
        Set<GroupRole> patientGroupRoles = new HashSet<>();
        patientGroupRoles.add(patientGroupRole);
        patientUser.setGroupRoles(patientGroupRoles);

        // new role
        Role newPatientRole = TestUtils.createRole(RoleName.PATIENT);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.PATIENT);
        newPatientRole.setRoleType(roleType);

        when(userRepository.findById(eq(patientUser.getId()))).thenReturn(Optional.of(patientUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(newPatientRole.getId()))).thenReturn(Optional.of(newPatientRole));
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(patientGroupRole);

        // add GroupRole to staff user
        groupRole = userService.addGroupRole(patientUser.getId(), group.getId(), newPatientRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, times(2)).save(any(GroupRole.class));

        // verify queued to RDC, must be members of Renal and UNIT group
        verify(externalServiceService, times(1))
                .addToQueue(eq(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION),
                        any(String.class), any(User.class), any(Date.class), any(GroupRole.class));

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
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.addInformation(user.getId(), userInformations);
        verify(userInformationRepository, times(1)).save(any(UserInformation.class));
    }

    @Test
    public void testAddPatientGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Group parent = TestUtils.createGroup("Renal");
        parent.setCode("Renal");
        // create group relationships
        group.getGroupRelationships().add(TestUtils.createGroupRelationship(group, parent, RelationshipTypes.PARENT));

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
        User patientUser = TestUtils.createUser("patient");
        patientUser.setDateOfBirth(new Date());

        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole patientGroupRole = TestUtils.createGroupRole(patientRole, group2, patientUser);
        Set<GroupRole> patientGroupRoles = new HashSet<>();
        patientGroupRoles.add(patientGroupRole);
        patientUser.setGroupRoles(patientGroupRoles);

        // new role
        Role newRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        // newly created grouprole
        group.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        GroupRole newGroupRole = TestUtils.createGroupRole(newRole, group, patientUser);

        when(userRepository.findById(eq(patientUser.getId()))).thenReturn(Optional.of(patientUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(newRole.getId()))).thenReturn(Optional.of(newRole));
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(newGroupRole);

        // add GroupRole to user
        groupRole = userService.addGroupRole(patientUser.getId(), group.getId(), newRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);
        verify(groupRoleRepository, times(2)).save(any(GroupRole.class));

        // verify queued to RDC, must be members of Renal and UNIT group
        verify(externalServiceService, times(1))
                .addToQueue(eq(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION), any(String.class),
                        any(User.class), any(Date.class), any(GroupRole.class));
    }

    @Test
    public void testChangeSecretWord() throws ResourceNotFoundException, ResourceForbiddenException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        SecretWordInput secretWordInput = new SecretWordInput("ABCDEFG", "ABCDEFG");

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.changeSecretWord(user.getId(), secretWordInput, false);

        verify(userRepository, times(1)).save(any(User.class));
        verify(userTokenRepository, times(1)).findByUser(user.getId());
    }

    @Test
    public void testChangeSecretWord_With_Old_Secret_Word_Success() throws ResourceNotFoundException, ResourceForbiddenException,
            NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String oldWord = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < oldWord.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(oldWord.charAt(i)) + salt));
        }
        user.setSecretWord(new JSONObject(letters).toString());

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        SecretWordInput secretWordInput = new SecretWordInput(oldWord, "ABCDEFG", "ABCDEFG");

        userService.changeSecretWord(user.getId(), secretWordInput, false);

        verify(authenticationService, times(1)).checkLettersAgainstSecretWord(any(User.class), any(Map.class), any(Boolean.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userTokenRepository, times(1)).findByUser(user.getId());
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testChangeSecretWord_With_Old_Secret_Word_Dont_Match() throws ResourceNotFoundException, ResourceForbiddenException,
            NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String oldWord = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < oldWord.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(oldWord.charAt(i)) + salt));
        }
        user.setSecretWord(new JSONObject(letters).toString());

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        doThrow(ResourceForbiddenException.class)
                .when(authenticationService)
                .checkLettersAgainstSecretWord(any(User.class), any(Map.class), any(Boolean.class));


        SecretWordInput secretWordInput = new SecretWordInput("Invalid", "ABCDEFG", "ABCDEFG");
        userService.changeSecretWord(user.getId(), secretWordInput, false);

        verify(authenticationService, times(1)).checkLettersAgainstSecretWord(any(User.class), any(Map.class), any(Boolean.class));
        verify(userRepository, times(0)).save(any(User.class));
        verify(userTokenRepository, times(0)).findByUser(user.getId());
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testChangeSecretWord_Missing_Old_Secret_Word() throws ResourceNotFoundException, ResourceForbiddenException,
            NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // Set old secret word for User
        // create secret word hashmap and convert to json to store in secret word
        // field, each letter is hashed
        String word = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }
        user.setSecretWord(new JSONObject(letters).toString());

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        // missing sold secret word from input
        SecretWordInput secretWordInput = new SecretWordInput("ABCDEFG", "ABCDEFG");

        userService.changeSecretWord(user.getId(), secretWordInput, false);
    }

    @Test
    public void testChangeSecretWord_returnSalt() throws ResourceNotFoundException, ResourceForbiddenException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        SecretWordInput secretWordInput = new SecretWordInput("ABCDEFG", "ABCDEFG");

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        String salt = userService.changeSecretWord(user.getId(), secretWordInput, true);

        verify(userRepository, times(1)).save(any(User.class));
        Assert.assertNotNull("Should return salt", salt);
    }

    @Test
    public void testIsSecretWordChanged_theSame() throws ResourceNotFoundException, ResourceForbiddenException,
            NoSuchAlgorithmException {

        //  generate secret word
        String salt = CommonUtils.generateSalt();
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        boolean isChanged = userService.isSecretWordChanged(user.getId(), salt);

        Assert.assertFalse("Salt should be the same", isChanged);
    }

    @Test
    public void testIsSecretWordChanged_changed() throws ResourceNotFoundException, ResourceForbiddenException,
            NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        //  generate secret word
        String salt = CommonUtils.generateSalt();
        String oldSalt = CommonUtils.generateSalt();
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        boolean isChanged = userService.isSecretWordChanged(user.getId(), oldSalt);

        Assert.assertTrue("Salt should be be different", isChanged);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testChangeSecretWord_notLetters() throws ResourceNotFoundException, ResourceForbiddenException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        SecretWordInput secretWordInput = new SecretWordInput("ABC1234", "ABC1234");

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.changeSecretWord(user.getId(), secretWordInput, false);
    }

    @Test
    public void testCreateUser() throws ResourceNotFoundException, ResourceForbiddenException, VerificationException, FhirResourceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(role2.getId()))).thenReturn(Optional.of(role2));
        when(groupRoleRepository.userGroupRoleExists(any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(false);
        when(groupRoleRepository.save(any(GroupRole.class))).thenReturn(groupRole2);

        userService.createUserWithPasswordEncryption(newUser);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCreateUserWrongGroup()
            throws ResourceNotFoundException, ResourceForbiddenException, VerificationException, FhirResourceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group2 = TestUtils.createGroup("testGroup2");
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRole);

        userService.createUserWithPasswordEncryption(newUser);
    }

    @Test
    public void testCurrentUserCanSwitchToUser_UnitStaff() {
        Group group1 = TestUtils.createGroup("test1Group");
        group1.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group2 = TestUtils.createGroup("test2Group");
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group3 = TestUtils.createGroup("test3Group");
        group3.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));

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
        group1.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group2 = TestUtils.createGroup("test2Group");
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group3 = TestUtils.createGroup("test3Group");
        group3.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));

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
    public void testDeleteFeature() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(featureRepository.findById(eq(feature.getId()))).thenReturn(Optional.of(feature));
        when(userFeatureRepository.findByUserAndFeature(user, feature)).thenReturn(userFeature);

        userService.deleteFeature(staffUser.getId(), feature.getId());
        verify(userFeatureRepository, times(1)).delete(any(UserFeature.class));
    }

    /**
     * Test: To remove a group role
     * Fail: The repository does not get called
     */
    @Test
    public void testDeleteGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRoleStaff);

        // add GroupRole to staff user
        userService.deleteGroupRole(staffUser.getId(), group.getId(), staffRole.getId());
        verify(groupRoleRepository, times(1)).delete(any(GroupRole.class));
    }

    @Test
    public void testDeletePatientGroupRole() throws ResourceNotFoundException, ResourceForbiddenException {

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
        User patientUser = TestUtils.createUser("staff");
        Group parent = TestUtils.createGroup("Renal");
        parent.setCode("Renal");
        // create group relationships
        group2.getGroupRelationships().add(TestUtils.createGroupRelationship(group2, parent, RelationshipTypes.PARENT));
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group2, patientUser);
        groupRolePatient.setCreated(new Date(new Date().getTime() - 1000000));
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patientUser.setGroupRoles(groupRolesPatient);
        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111");

        when(userRepository.findById(eq(patientUser.getId()))).thenReturn(Optional.of(patientUser));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));
        when(roleRepository.findById(eq(patientRole.getId()))).thenReturn(Optional.of(patientRole));
        when(groupRoleRepository.findByUserGroupRole(any(User.class), any(Group.class), any(Role.class)))
                .thenReturn(groupRolePatient);

        // add GroupRole to staff user
        group.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        group2.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        userService.deleteGroupRole(patientUser.getId(), group.getId(), patientRole.getId());
        verify(groupRoleRepository, times(1)).delete(any(GroupRole.class));

        // verify queued to RDC, must be members of Renal and UNIT group
        verify(externalServiceService, times(1))
                .addToQueue(eq(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION), any(String.class),
                        any(User.class), any(Date.class), any(GroupRole.class));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));

        userService.delete(staffUser.getId(), false);
        verify(deletePatientTask, times(1)).deletePatient(any(User.class), any(User.class));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));

        userService.delete(staffUser.getId(), false);
        verify(userRepository, times(0)).delete(any(User.class));
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

        verify(identifierRepository, times(1)).findByValue(eq(identifier.getIdentifier()));
        Assert.assertNotNull("The user should be returned", user2);
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
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.getInformation(user.getId());
        verify(userInformationRepository, times(1)).findByUser(any(User.class));
    }

    @Test
    public void testHideSecretWordNotification() throws ResourceNotFoundException, ResourceForbiddenException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.hideSecretWordNotification(user.getId());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testListDuplicateGroupRoles() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        User patient = TestUtils.createUser("patient");
        Group patientGroup = TestUtils.createGroup("patientGroup");
        patientGroup.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole originalGroupRole = TestUtils.createGroupRole(patientRole, patientGroup, patient);
        GroupRole differentGroupRole = TestUtils.createGroupRole(staffRole, patientGroup, patient);
        GroupRole duplicateGroupRole = TestUtils.createGroupRole(patientRole, patientGroup, patient);

        when(groupRepository.findAll()).thenReturn(Arrays.asList(group, patientGroup));
        when(groupRoleRepository.findByGroup(eq(group))).thenReturn(Collections.singletonList(groupRole));
        when(groupRoleRepository.findByGroup(eq(patientGroup)))
                .thenReturn(Arrays.asList(originalGroupRole, differentGroupRole, duplicateGroupRole));

        String result = userService.listDuplicateGroupRoles();

        Assert.assertTrue("Should return correct ID in list", result.contains(duplicateGroupRole.getId().toString()));
        Assert.assertEquals("String returned should be correct",
                "(" + duplicateGroupRole.getId().toString() + ")", result);

        verify(groupRepository, times(1)).findAll();
        verify(groupRoleRepository, times(1)).findByGroup(eq(group));
        verify(groupRoleRepository, times(1)).findByGroup(eq(patientGroup));
    }

    /**
     * Test: Password reset check
     * Fail: Service is not called and the change password flag is not set
     */
    @Test
    public void testPasswordReset() throws ResourceNotFoundException, ResourceForbiddenException, MessagingException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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
        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.save(eq(staffUser))).thenReturn(staffUser);

        org.patientview.api.model.User user1 = userService.resetPassword(staffUser.getId(), password);

        verify(userRepository, times(1)).findById(eq(staffUser.getId()));
        verify(userRepository, times(1)).save(eq(staffUser));
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

        // mock security context
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new TestAuthentication(user));
        SecurityContextHolder.setContext(context);

        String password = "newPassword";

        user.setChangePassword(Boolean.TRUE);
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        userService.changePassword(user.getId(), password);
        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(userTokenRepository, times(1)).findByUser(user.getId());
    }

    @Test
    public void testRemoveSecretWord() throws ResourceForbiddenException, ResourceNotFoundException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userService.removeSecretWord(user.getId());

        verify(userRepository, times(1)).save(eq(user));
    }

    /**
     * Test: User has forgotten password. Update a user with a new password and set the change flag.
     * Fail: Does not find the Resource
     *
     * @throws ResourceNotFoundException
     */
    @Test
    public void testResetPassword() throws ResourceNotFoundException, MailException, MessagingException,
            ResourceForbiddenException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(user);
        when(captchaService.verify(any(String.class))).thenReturn(true);
        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail(), "capture");

        verify(emailService, times(1)).sendEmail(any(Email.class));
        verify(userRepository, times(1)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: User has forgotten password. Update a user with a new password but the user's email is wrong
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testResetPassword_WrongEmail() throws ResourceNotFoundException, MailException, MessagingException,
            ResourceForbiddenException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(user);
        when(captchaService.verify(any(String.class))).thenReturn(true);
        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail", "capture");

        verify(emailService, times(0)).sendEmail(any(Email.class));
        verify(userRepository, times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    /**
     * Test: User has forgotten password. Update a user with a new password but the username does not exist
     * Fail: Does not throw an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testResetPassword_WrongUsername() throws ResourceNotFoundException, MailException, MessagingException,
            ResourceForbiddenException {
        String email = "forgotten@email.co.uk";
        User user = TestUtils.createUser("testForgottenPassword");
        user.setEmail(email);
        when(userRepository.findByUsernameCaseInsensitive(eq(user.getUsername()))).thenReturn(null);
        when(captchaService.verify(any(String.class))).thenReturn(true);
        userService.resetPasswordByUsernameAndEmail(user.getUsername(), user.getEmail() + "fail", "capture");

        verify(emailService, times(0)).sendEmail(any(Email.class));
        verify(userRepository, times(0)).save(eq(user));
        Assert.assertTrue("The set change password is set", user.getChangePassword() == Boolean.TRUE);
    }

    @Test
    public void testSendVerificationEmail()
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(properties.getProperty((eq("smtp.sender")))).thenReturn("test@solidstategroup.com");

        userService.sendVerificationEmail(staffUser.getId());
        verify(emailService, times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void testUndelete() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to undelete
        User staffUser = TestUtils.createUser("staff");
        staffUser.setDeleted(true);
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);

        userService.undelete(staffUser.getId());

        verify(userRepository, times(1)).findById(eq(staffUser.getId()));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testUndelete_patient() throws ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to save
        User staffUser = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);

        userService.undelete(staffUser.getId());
    }

    @Test
    public void testUpdateUser() throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));

        userService.save(staffUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUserEmailAlreadyExistsCheck() throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");

        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to save
        User staffUser = TestUtils.createUser("staff");
        User staffUser2 = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findById(eq(staffUser2.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));

        staffUser2.setEmail("newemail@patientview.org");
        userService.save(staffUser2);
        verify(userRepository, times(1)).emailExistsCaseInsensitive(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test(expected = EntityExistsException.class)
    public void testUpdateUserEmailAlreadyExistsFailure() throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

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
        User staffUser2 = TestUtils.createUser("staff");
        Role staffRole = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(staffRole, group, staffUser);
        Set<GroupRole> groupRolesStaff = new HashSet<>();
        groupRolesStaff.add(groupRoleStaff);
        staffUser.setGroupRoles(groupRolesStaff);

        when(userRepository.findById(eq(staffUser2.getId()))).thenReturn(Optional.of(staffUser));
        when(userRepository.emailExistsCaseInsensitive(eq("newemail@patientview.org"))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(roleRepository.findById(eq(role.getId()))).thenReturn(Optional.of(role));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));

        staffUser2.setEmail("newemail@patientview.org");
        userService.save(staffUser2);
        verify(userRepository, times(1)).emailExistsCaseInsensitive(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testUpdateUserWrongGroup()
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Group group2 = TestUtils.createGroup("testGroup2");
        group2.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Role role = TestUtils.createRole(RoleName.STAFF_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
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

        when(userRepository.findById(eq(staffUser.getId()))).thenReturn(Optional.of(staffUser));
        when(roleRepository.findById(eq(staffRole.getId()))).thenReturn(Optional.of(staffRole));
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));

        userService.save(staffUser);
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
        verify(userRepository, times(1)).findByUsernameCaseInsensitive(eq(staffUser.getUsername()));
    }

    @Test
    public void testGetUserStats() throws FhirResourceException, ResourceNotFoundException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(conversationService.getUnreadConversationCount(user.getId())).thenReturn(10L);
        when(apiMedicationService.getByUserId(user.getId())).thenReturn(new ArrayList<FhirMedicationStatement>());

        Map<String, Integer> stats = userService.getUserStats(user.getId());
        Assert.assertNotNull("Stats map should have been returned", stats);
    }

    /**
     * Helper class to help mock security context
     */
    private class TestAuthentication extends AbstractAuthenticationToken {
        private final UserDetails principal;

        public TestAuthentication(UserDetails principal) {
            super(principal.getAuthorities());
            this.principal = principal;
        }

        @Override
        public UserToken getCredentials() {
            return new UserToken();
        }

        @Override
        public UserDetails getPrincipal() {
            return principal;
        }
    }
}
