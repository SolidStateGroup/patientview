package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.IdentifierServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import javax.persistence.EntityExistsException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;
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
    LookupRepository lookupRepository;

    @InjectMocks
    IdentifierService identifierService = new IdentifierServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @Test
    public void testUpdateIdentifier() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
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

        when(identifierRepository.findOne(eq(identifier.getId()))).thenReturn(identifier);
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);
        when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

        try {
            identifierService.save(identifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).save(eq(identifier));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testUpdateIdentifierWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
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

        when(identifierRepository.findOne(eq(identifier.getId()))).thenReturn(identifier);
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);
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

        when(identifierRepository.findOne(eq(identifier.getId()))).thenReturn(identifier);
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);

        try {
            identifierService.delete(identifier.getId());
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).delete(eq(identifier.getId()));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testDeleteIdentifierWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
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

        when(identifierRepository.findOne(eq(identifier.getId()))).thenReturn(identifier);
        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);

        try {
            identifierService.delete(identifier.getId());
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            throw e;
        }

        verify(identifierRepository, Mockito.times(1)).delete(eq(identifier.getId()));
    }

    /**
     * Test: To create an identifier on a user record
     * Fail: Identifier does not get created
     */
    @Test
    public void testAddIdentifier() {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
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

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifier);
        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);

        try {
            identifierService.add(patient.getId(), identifier);
        } catch (ResourceForbiddenException | ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        verify(identifierRepository, Mockito.times(1)).save(Matchers.eq(identifier));
    }

    /**
     * Test: To create a duplicate identifier on a user record
     * Fail: Error not thrown
     */
    @Test(expected = EntityExistsException.class)
    public void testAddDuplicateIdentifier()
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        Long userId = 1L;
        User user = TestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());

        User user2 = TestUtils.createUser("testUser2");
        user2.setIdentifiers(new HashSet<Identifier>());

        Identifier identifier = new Identifier();
        identifier.setId(3L);
        identifier.setIdentifier("1111111111");
        identifier.setUser(user);

        Identifier identifier2 = new Identifier();
        identifier2.setId(4L);
        identifier2.setIdentifier("1111111111");
        identifier2.setUser(user2);

        when(userRepository.findOne(Matchers.eq(userId))).thenReturn(user);
        when(identifierRepository.findByValue(identifier.getIdentifier())).thenReturn(identifier2);

        identifierService.add(userId, identifier);
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
        Identifier foundIdentifier = identifierService.getIdentifierByValue(identifier.getIdentifier());
        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
        Assert.assertTrue("Identifier should be found", foundIdentifier != null);
    }
}
