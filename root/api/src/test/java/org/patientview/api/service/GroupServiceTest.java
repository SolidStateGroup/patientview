package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.model.Email;
import org.patientview.api.model.UnitRequest;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupServiceTest {

    @Mock
    EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private GroupRelationshipRepository groupRelationshipRepository;

    @Mock
    private GroupFeatureRepository groupFeatureRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GroupService groupService = new GroupServiceImpl();

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditAspect auditAspect = AuditAspect.aspectOf();

    private User creator;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    /**
     * Test: The creation of the parent and child groups
     * Fail: The the parent and child groups are not returned
     *
     */
    @Test

    public void testAddGroupChildAndParent() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
;
        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup  = TestUtils.createGroup("childGroup");
        List<Group> childGroups = new ArrayList<>();
        List<Group> parentGroups = new ArrayList<>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.findByName(Matchers.eq(testGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(childGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(parentGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Group group = groupService.add(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group feature has been created", group);
    }

    /**
     * Test: The creation of the parent and child groups
     * Fail: The the parent and child groups are not returned
     *
     */
    @Test
    public void testAddGroupChildAndParentOnCreate() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup  = TestUtils.createGroup("childGroup");
        List<Group> childGroups = new ArrayList<>();
        List<Group> parentGroups = new ArrayList<>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.findByName(Matchers.eq(testGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(childGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(parentGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Group group = groupService.add(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group feature has been created", group);
    }

    @Test
    public void testAddGroupFeature() {
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature testFeature = TestUtils.createFeature(FeatureType.MESSAGING.getName());
        GroupFeature groupFeature = TestUtils.createGroupFeature(testFeature, testGroup);

        // add user as specialty admin to group
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, testUser);
        testUser.setGroupRoles(new TreeSet<GroupRole>());
        testUser.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(testUser, testUser.getGroupRoles());

        testGroup.setGroupFeatures(new HashSet<GroupFeature>());
        testGroup.getGroupFeatures().add(groupFeature);

        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(featureRepository.findOne(Matchers.eq(testFeature.getId()))).thenReturn(testFeature);
        when(groupFeatureRepository.save(Matchers.any(GroupFeature.class))).thenReturn(groupFeature);

        groupService.addFeature(testGroup.getId(), testFeature.getId());

        Assert.assertNotNull("The returned object should not be null", groupRepository.findOne(testGroup.getId()).getGroupFeatures());

        verify(groupFeatureRepository, Mockito.times(1)).save(Matchers.any(GroupFeature.class));
    }

    @Test
    public void testRemoveGroupFeature() {
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature testFeature = TestUtils.createFeature(FeatureType.MESSAGING.getName());
        GroupFeature groupFeature = TestUtils.createGroupFeature(testFeature, testGroup);

        // add user as specialty admin to group
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, testUser);
        testUser.setGroupRoles(new TreeSet<GroupRole>());
        testUser.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(testUser, testUser.getGroupRoles());

        testGroup.setGroupFeatures(new HashSet<GroupFeature>());
        testGroup.getGroupFeatures().add(groupFeature);

        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(featureRepository.findOne(Matchers.eq(testFeature.getId()))).thenReturn(testFeature);
        when(groupFeatureRepository.save(Matchers.any(GroupFeature.class))).thenReturn(groupFeature);

        groupService.deleteFeature(testGroup.getId(), testFeature.getId());
        testGroup.getGroupFeatures().remove(groupFeature);

        Assert.assertEquals("There should be no group features", 0, groupRepository.findOne(testGroup.getId()).getGroupFeatures().size());

        verify(groupFeatureRepository, Mockito.times(1)).delete(Matchers.any(GroupFeature.class));
    }

    /**
     * Test: Create a parent relationship between to group objects
     * Fail: The parent and child relationship are not persisted
     *
     */
    @Test
    public void testAddParentGroup() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        Group testGroup = TestUtils.createGroup("testGroup");
        Group testParentGroup = TestUtils.createGroup("testGroup");

        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.findOne(Matchers.eq(testParentGroup.getId()))).thenReturn(testParentGroup);

        groupService.addParentGroup(testGroup.getId(), testParentGroup.getId());

        // Parent and child relationship should be persisted
        verify(groupRelationshipRepository, Mockito.times(2)).save(Matchers.any(GroupRelationship.class));

        Assert.assertNotNull("They should the correct GroupObject returned");
    }

    /**
     * Test: create parent and 2 children, get parent group and its children based on a user's membership of the parent group
     */
    @Test
    @Ignore
    // todo: finish checking list of groups against specialty admin
    public void testFindGroupAndChildGroupsByUser() {

        // create user
        User testUser = TestUtils.createUser("testUser");

        // create groups
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup1  = TestUtils.createGroup("childGroup1");
        Group childGroup2  = TestUtils.createGroup("childGroup2");
        List<Group> childGroups = new ArrayList<>();
        childGroups.add(childGroup1);
        childGroups.add(childGroup2);
        parentGroup.setChildGroups(childGroups);
        List<Group> allGroups = new ArrayList<Group>();
        allGroups.add(parentGroup);

        // add user as specialty admin to group
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, parentGroup, testUser);
        testUser.setGroupRoles(new TreeSet<GroupRole>());
        testUser.getGroupRoles().add(groupRole);
        List<Role> roles = new ArrayList<Role>();
        roles.add(role);


        // create group relationships
        Set<GroupRelationship> groupRelationships = new HashSet<GroupRelationship>();
        GroupRelationship child1 =  TestUtils.createGroupRelationship(parentGroup, childGroup1, RelationshipTypes.CHILD);
        GroupRelationship child2 =  TestUtils.createGroupRelationship(parentGroup, childGroup2, RelationshipTypes.CHILD);
        groupRelationships.add(child1);
        groupRelationships.add(child2);
        parentGroup.setGroupRelationships(groupRelationships);

        // setup stubbing
        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(roleRepository.findByUser(Matchers.eq(testUser))).thenReturn(roles);
        when(groupRepository.findOne(Matchers.eq(parentGroup.getId()))).thenReturn(parentGroup);
        when(groupRepository.findGroupByUser(Matchers.eq(testUser))).thenReturn(allGroups);
        when(groupRepository.save(Matchers.eq(parentGroup))).thenReturn(parentGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        try {
            Group group = groupService.save(parentGroup);
            //Assert.assertEquals("Should retrieve 3 groups", 3, securityService.getUserGroups(testUser.getId()).size());
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            Assert.fail("Exception thrown");
        }
    }


    /**
     * Test: To simple call to the repository to retrieve child groups
     *
     */
    @Test
    public void testFindChildGroups() throws ResourceNotFoundException {

        // Set up groups
        Group testGroup = TestUtils.createGroup("testGroup");
        Group childGroup = TestUtils.createGroup("childGroup");

        List<Group> childGroups = new ArrayList<>();

        childGroups.add(childGroup);

        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.findChildren(eq(testGroup))).thenReturn(childGroups);

        childGroups = groupService.findChildren(testGroup.getId());
        Assert.assertFalse("There should be child objects", CollectionUtils.isEmpty(childGroups));

    }

    /**
     * Test: To simple call to the repository to retrieve child groups with an invalid group id
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testFindChildGroups_Exception() throws ResourceNotFoundException {

        // Set up groups
        Group testGroup = TestUtils.createGroup("testGroup");
        Group childGroup = TestUtils.createGroup("childGroup");

        List<Group> childGroups = new ArrayList<>();

        childGroups.add(childGroup);

        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(null);
        when(groupRepository.findChildren(eq(testGroup))).thenReturn(childGroups);

        childGroups = groupService.findChildren(testGroup.getId());

        verify(groupRepository, Mockito.times(1)).findChildren(eq(testGroup));
        Assert.assertFalse("There should be child objects", CollectionUtils.isEmpty(childGroups));

    }

    /**
     * Test: Password Request / Contact Unit functionality.
     * Fail: Doesnt send an email of any type
     */
    @Test
    public void testPasswordRequest() throws Exception {
        UnitRequest unitRequest = new UnitRequest();
        unitRequest.setNhsNumber("234234234");
        unitRequest.setDateOfBirth(new Date());
        unitRequest.setForename("forename");
        unitRequest.setSurname("surname");

        Group group = TestUtils.createGroup("TestGroup");
        group.setContactPoints(new HashSet<ContactPoint>());
        group.getContactPoints().add(TestUtils.createContactPoint("83", ContactPointTypes.PV_ADMIN_EMAIL));
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        groupService.passwordRequest(group.getId(), unitRequest);

        verify(groupRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
    }

    /**
     * Test: Password Request / Contact Unit functionality with no email address
     * Fail: Doesn't raise an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testPasswordRequest_NotContactEmail() throws Exception {
        UnitRequest unitRequest = new UnitRequest();
        unitRequest.setNhsNumber("234234234");
        unitRequest.setDateOfBirth(new Date());
        unitRequest.setForename("forename");
        unitRequest.setSurname("surname");

        Group group = TestUtils.createGroup("TestGroup");
        group.setContactPoints(new HashSet<ContactPoint>());
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        groupService.passwordRequest(group.getId(), unitRequest);

        verify(groupRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
    }

    /**
     * Test: Password Request / Contact Unit functionality with a valid unit
     * Fail: Doesnt raise an exception
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testContactUnit_NoGroupExists() throws Exception {
        UnitRequest unitRequest = new UnitRequest();
        unitRequest.setNhsNumber("234234234");
        unitRequest.setDateOfBirth(new Date());
        unitRequest.setForename("forename");
        unitRequest.setSurname("surname");

        Group group = TestUtils.createGroup("TestGroup");
        group.setContactPoints(new HashSet<ContactPoint>());
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(null);
        groupService.passwordRequest(group.getId(), unitRequest);

        verify(groupRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
    }
}
