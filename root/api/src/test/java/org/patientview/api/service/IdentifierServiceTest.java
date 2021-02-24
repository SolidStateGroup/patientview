package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.UserIdentifier;
import org.patientview.api.service.impl.IdentifierServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/10/2014
 */
public class IdentifierServiceTest {

    User creator;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Mock
    LookupRepository lookupRepository;

    @InjectMocks
    IdentifierService identifierService = new IdentifierServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testUpdateIdentifier() {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findById(eq(identifier.getId()))).thenReturn(Optional.of(identifier));
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);
        when(lookupRepository.findById(any(Long.class))).thenReturn(Optional.of(lookup));
        when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

        try {
            identifierService.save(identifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).save(eq(identifier));
        verify(userService, Mockito.times(1)).sendUserUpdatedGroupNotification(any(User.class), any(Boolean.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testUpdateIdentifierWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findById(eq(identifier.getId()))).thenReturn(Optional.of(identifier));
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);
        when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

        try {
            identifierService.save(identifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            throw e;
        }
    }

    @Test
    public void testDeleteIdentifier() {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findById(eq(identifier.getId()))).thenReturn(Optional.of(identifier));
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);

        try {
            identifierService.delete(identifier.getId());
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).deleteById(eq(identifier.getId()));
        verify(userService, Mockito.times(1)).sendUserUpdatedGroupNotification(any(User.class), any(Boolean.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testDeleteIdentifierWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findById(eq(identifier.getId()))).thenReturn(Optional.of(identifier));
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);

        try {
            identifierService.delete(identifier.getId());
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            throw e;
        }

        verify(identifierRepository, Mockito.times(1)).deleteById(eq(identifier.getId()));
    }

    /**
     * Test: To create an identifier on a user record
     * Fail: Identifier does not get created
     */
    @Test
    public void testAddIdentifier() {
        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);
        when(lookupRepository.findById(any(Long.class))).thenReturn(Optional.of(lookup));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));
        when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

        try {
            identifierService.add(patient.getId(), identifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).save(Matchers.eq(identifier));
        verify(userService, Mockito.times(1)).sendUserUpdatedGroupNotification(any(User.class), any(Boolean.class));
    }

    /**
     * Test: To create a duplicate identifier on a user record
     * Fail: Error not thrown
     */
    @Test(expected = EntityExistsException.class)
    public void testAddDuplicateIdentifier()
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Role patientRole = TestUtils.createRole(RoleName.PATIENT);

        Long userId = 1L;
        User user1 = TestUtils.createUser("testUser");
        GroupRole groupRole1 = TestUtils.createGroupRole(patientRole, group, user1);
        Set<GroupRole> groupRoles1 = new HashSet<>();
        groupRoles1.add(groupRole1);
        user1.setGroupRoles(groupRoles1);
        user1.setIdentifiers(new HashSet<Identifier>());

        User user2 = TestUtils.createUser("testUser2");
        GroupRole groupRole2 = TestUtils.createGroupRole(patientRole, group, user2);
        Set<GroupRole> groupRoles2 = new HashSet<>();
        groupRoles2.add(groupRole2);
        user2.setGroupRoles(groupRoles2);
        user2.setIdentifiers(new HashSet<Identifier>());

        Identifier identifier = new Identifier();
        identifier.setId(3L);
        identifier.setIdentifier("1111111111");
        identifier.setUser(user1);

        Identifier identifier2 = new Identifier();
        identifier2.setId(4L);
        identifier2.setIdentifier("1111111111");
        identifier2.setUser(user2);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier2);

        when(userRepository.findById(Matchers.eq(userId))).thenReturn(Optional.of(user1));
        when(identifierRepository.findByValue(identifier.getIdentifier())).thenReturn(identifiers);

        identifierService.add(userId, identifier);
    }

    /**
     * Test: Get identifier by value
     * Fail: Service is not called
     */
    @Test
    public void testGetIdentifierByValue() throws ResourceNotFoundException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        String identifierValue = "1111111111";
        Identifier identifier = new Identifier();
        identifier.setIdentifier(identifierValue);
        identifier.setId(1L);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);

        List<Identifier> foundIdentifiers = identifierService.getIdentifierByValue(identifier.getIdentifier());

        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
        Assert.assertFalse("Identifier should be found", CollectionUtils.isEmpty(foundIdentifiers));
    }

    @Test
    public void testValidateIdentifier_NhsNumber() {
        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.NHS_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "4173743890");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        try {
            identifierService.validate(userIdentifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException
                | EntityExistsException | ResourceInvalidException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidateIdentifier_DummyNhsNumber() {
        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.NHS_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "abc123def4");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(true);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        try {
            identifierService.validate(userIdentifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException
                | EntityExistsException | ResourceInvalidException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(expected = ResourceInvalidException.class)
    public void testValidateIdentifier_InvalidNhsNumber() throws ResourceForbiddenException, ResourceNotFoundException,
            EntityExistsException, ResourceInvalidException {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.NHS_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "4176743890");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        identifierService.validate(userIdentifier);
    }

    @Test
    public void testValidateIdentifier_ChiNumber() {
        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.CHI_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.CHI_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "2000000002");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        try {
            identifierService.validate(userIdentifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException
                | EntityExistsException | ResourceInvalidException e) {
            fail("Exception: " + e.getMessage());
        }
    }


    @Test(expected = ResourceInvalidException.class)
    public void testValidateIdentifier_InvalidChiNumber() throws ResourceForbiddenException, ResourceNotFoundException,
            EntityExistsException, ResourceInvalidException {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.CHI_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.CHI_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "101256420");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        identifierService.validate(userIdentifier);
    }

    @Test
    public void testValidateIdentifier_HscNumber() {
        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.HSC_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.HSC_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "3340219001");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        try {
            identifierService.validate(userIdentifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException
                | EntityExistsException | ResourceInvalidException e) {
            fail("Exception: " + e.getMessage());
        }
    }


    @Test(expected = ResourceInvalidException.class)
    public void testValidateIdentifier_InvalidHscNumber() throws ResourceForbiddenException, ResourceNotFoundException,
            EntityExistsException, ResourceInvalidException {

        // user and security
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

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.HSC_NUMBER.toString());
        lookup.setDescription(IdentifierTypes.HSC_NUMBER.getName());

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "4000000004");

        // transport object
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUserId(patient.getId());
        userIdentifier.setIdentifier(identifier);
        userIdentifier.setDummy(false);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(null);
        when(lookupRepository.findById(eq(identifier.getIdentifierType().getId())))
                .thenReturn(Optional.of(identifier.getIdentifierType()));
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));

        identifierService.validate(userIdentifier);
    }
}
