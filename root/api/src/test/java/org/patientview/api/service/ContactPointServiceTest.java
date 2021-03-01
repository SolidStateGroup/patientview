package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ContactPointServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.test.util.TestUtils;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/10/2014
 */
public class ContactPointServiceTest {

    User creator;

    @Mock
    ContactPointRepository contactPointRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    CodeRepository codeRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    ContactPointService contactPointService = new ContactPointServiceImpl();

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
    public void testCreateContactPoint() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        ContactPoint contactPoint = TestUtils.createContactPoint("TestContactPoint", ContactPointTypes.PV_ADMIN_EMAIL);
        contactPoint.setGroup(group);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(contactPointRepository.save(eq(contactPoint))).thenReturn(contactPoint);
        when(entityManager.find(eq(ContactPointType.class), eq(contactPoint.getContactPointType().getId())))
                .thenReturn(contactPoint.getContactPointType());

        try {
            contactPointService.add(group.getId(), contactPoint);
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            fail("Exception: " + e.getMessage());
        }

        Assert.assertNotNull("The returned contactPoint should not be null", contactPoint);
        verify(contactPointRepository, Mockito.times(1)).save(eq(contactPoint));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCreateContactPointWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

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

        ContactPoint contactPoint = TestUtils.createContactPoint("TestContactPoint", ContactPointTypes.PV_ADMIN_EMAIL);
        contactPoint.setGroup(group);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(contactPointRepository.save(eq(contactPoint))).thenReturn(contactPoint);
        when(entityManager.find(eq(ContactPointType.class), eq(contactPoint.getContactPointType().getId())))
                .thenReturn(contactPoint.getContactPointType());

        contactPointService.add(group.getId(), contactPoint);

        Assert.assertNotNull("The returned contactPoint should not be null", contactPoint);
        verify(contactPointRepository, Mockito.times(1)).save(eq(contactPoint));
    }

    @Test
    public void testDeleteContactPoint() {

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

        ContactPoint contactPoint = TestUtils.createContactPoint("TestContactPoint", ContactPointTypes.PV_ADMIN_EMAIL);
        contactPoint.setGroup(group);

        group.setContactPoints(new HashSet<ContactPoint>());
        group.getContactPoints().add(contactPoint);

        when(contactPointRepository.findById(eq(contactPoint.getId()))).thenReturn(Optional.of(contactPoint));

        try {
            contactPointService.delete(contactPoint.getId());
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testDeleteContactPointWrongGroup() throws ResourceNotFoundException, ResourceForbiddenException {

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

        ContactPoint contactPoint = TestUtils.createContactPoint("TestContactPoint", ContactPointTypes.PV_ADMIN_EMAIL);
        contactPoint.setGroup(group);

        group.setContactPoints(new HashSet<ContactPoint>());
        group.getContactPoints().add(contactPoint);

        when(contactPointRepository.findById(eq(contactPoint.getId()))).thenReturn(Optional.of(contactPoint));

        contactPointService.delete(contactPoint.getId());
    }
}
