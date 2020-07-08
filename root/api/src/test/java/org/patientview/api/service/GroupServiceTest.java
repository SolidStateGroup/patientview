package org.patientview.api.service;

import org.junit.After;
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
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
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
import org.patientview.service.AuditService;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupServiceTest {

    @Mock
    private Properties properties;

    @Mock
    private EmailService emailService;

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

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: The creation of the parent and child groups
     * Fail: The the parent and child groups are not returned
     */
    @Test

    public void testAddGroupChildAndParent() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        ;
        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup = TestUtils.createGroup("childGroup");
        List<Group> childGroups = new ArrayList<>();
        List<Group> parentGroups = new ArrayList<>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        when(groupRepository.findById(Matchers.eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(groupRepository.findByName(Matchers.eq(testGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(childGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(parentGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Long groupId = groupService.add(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group has been created", groupId);
    }

    /**
     * Test: The creation of the parent and child groups
     * Fail: The the parent and child groups are not returned
     */
    @Test
    public void testAddGroupChildAndParentOnCreate() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup = TestUtils.createGroup("childGroup");
        List<Group> childGroups = new ArrayList<>();
        List<Group> parentGroups = new ArrayList<>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        when(groupRepository.findById(Matchers.eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(groupRepository.findByName(Matchers.eq(testGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(childGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.findByName(Matchers.eq(parentGroup.getName()))).thenReturn(new ArrayList<Group>());
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Long groupId = groupService.add(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group has been created", groupId);
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

        when(groupRepository.findById(Matchers.eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(featureRepository.findById(Matchers.eq(testFeature.getId()))).thenReturn(Optional.of(testFeature));
        when(groupFeatureRepository.save(Matchers.any(GroupFeature.class))).thenReturn(groupFeature);

        groupService.addFeature(testGroup.getId(), testFeature.getId());

        Assert.assertNotNull("The returned object should not be null",
                groupRepository.findById(testGroup.getId()).get().getGroupFeatures());

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

        when(groupRepository.findById(Matchers.eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(featureRepository.findById(Matchers.eq(testFeature.getId()))).thenReturn(Optional.of(testFeature));
        when(groupFeatureRepository.save(Matchers.any(GroupFeature.class))).thenReturn(groupFeature);

        groupService.deleteFeature(testGroup.getId(), testFeature.getId());
        testGroup.getGroupFeatures().remove(groupFeature);

        Assert.assertEquals("There should be no group features", 0,
                groupRepository.findById(testGroup.getId()).get().getGroupFeatures().size());

        verify(groupFeatureRepository, Mockito.times(1)).delete(Matchers.any(GroupFeature.class));
    }

    /**
     * Test: Create a parent relationship between to group objects
     * Fail: The parent and child relationship are not persisted
     */
    @Test
    public void testAddParentGroup() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        Group testGroup = TestUtils.createGroup("testGroup");
        Group testParentGroup = TestUtils.createGroup("testGroup");

        when(groupRepository.findById(Matchers.eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(Matchers.eq(testParentGroup.getId()))).thenReturn(Optional.of(testParentGroup));

        groupService.addParentGroup(testGroup.getId(), testParentGroup.getId());

        // Parent and child relationship should be persisted
        verify(groupRelationshipRepository, Mockito.times(2)).save(Matchers.any(GroupRelationship.class));

        Assert.assertNotNull("They should the correct GroupObject returned");
    }

    /**
     * Test: create parent and 2 children, get parent group and its children based on a user's membership of the parent
     * group
     */
    @Test
    @Ignore
    // todo: finish checking list of groups against specialty admin
    public void testFindGroupAndChildGroupsByUser() {

        // create user
        User testUser = TestUtils.createUser("testUser");

        // create groups
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup1 = TestUtils.createGroup("childGroup1");
        Group childGroup2 = TestUtils.createGroup("childGroup2");
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
        GroupRelationship child1 = TestUtils.createGroupRelationship(parentGroup, childGroup1, RelationshipTypes.CHILD);
        GroupRelationship child2 = TestUtils.createGroupRelationship(parentGroup, childGroup2, RelationshipTypes.CHILD);
        groupRelationships.add(child1);
        groupRelationships.add(child2);
        parentGroup.setGroupRelationships(groupRelationships);

        // setup stubbing
        when(userRepository.findById(Matchers.eq(testUser.getId()))).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUser(Matchers.eq(testUser))).thenReturn(roles);
        when(groupRepository.findById(Matchers.eq(parentGroup.getId()))).thenReturn(Optional.of(parentGroup));
        when(groupRepository.findGroupByUser(Matchers.eq(testUser))).thenReturn(allGroups);
        when(groupRepository.save(Matchers.eq(parentGroup))).thenReturn(parentGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        try {
            groupService.save(parentGroup);
            //Assert.assertEquals("Should retrieve 3 groups", 3, securityService.getUserGroups(testUser.getId()).size());
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            Assert.fail("Exception thrown");
        }
    }

    /**
     * Test: To simple call to the repository to retrieve child groups
     */
    @Test
    public void testFindChildGroups() throws ResourceNotFoundException {

        // Set up groups
        Group testGroup = TestUtils.createGroup("testGroup");
        Group childGroup = TestUtils.createGroup("childGroup");

        List<Group> childGroups = new ArrayList<>();

        childGroups.add(childGroup);

        when(groupRepository.findById(eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
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

        when(groupRepository.findById(eq(testGroup.getId()))).thenReturn(Optional.of(testGroup));
        when(groupRepository.findChildren(eq(testGroup))).thenReturn(childGroups);

        childGroups = groupService.findChildren(testGroup.getId());

        verify(groupRepository, Mockito.times(1)).findChildren(eq(testGroup));
        Assert.assertFalse("There should be child objects", CollectionUtils.isEmpty(childGroups));
    }

    /**
     * Test: Call the find all method if a User has the superadmin role
     * Fail: Find All was not called (SuperAdmin -> findAll, Anyone else -> findGroupsByUser)
     *
     * @return
     */
    @Test
    public void testGetUserGroupsWithSuperAdmin() {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        groupService.getUserGroups(user.getId(), new GetParameters());

        String filterText = "%%";
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        verify(groupRepository, Mockito.times(1)).findAll(filterText, pageable);
    }

    /**
     * Test: Call the findGroupsByUser method if a User does not have a globaladmin role
     * Fail: FindGroupByUser was not called (SuperAdmin -> findAll, Anyone else -> findGroupsByUser)
     *
     * @return
     */
    @Test
    public void testGetUserGroups() {
        GetParameters getParameters = new GetParameters();
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        groupService.getUserGroups(user.getId(), getParameters);

        String filterText = "%%";
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        verify(groupRepository, Mockito.times(1)).findGroupsByUserNoSpecialties(filterText, user, pageable);
    }

    @Test
    public void testGetByFeature() throws ResourceNotFoundException, ResourceForbiddenException {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        List<Group> groups = new ArrayList<>();
        groups.add(group);

        Feature feature = TestUtils.createFeature(FeatureType.MESSAGING.toString());
        when(groupRepository.findByFeature(eq(feature))).thenReturn(groups);
        when(featureRepository.findByName(eq(feature.getName()))).thenReturn(feature);

        List<org.patientview.api.model.Group> foundGroups = groupService.getByFeature(FeatureType.MESSAGING.toString());
        Assert.assertTrue("There should be returned Groups", !CollectionUtils.isEmpty(foundGroups));

        verify(groupRepository, Mockito.times(1)).findByFeature(feature);
        verify(featureRepository, Mockito.times(1)).findByName(feature.getName());
    }

}
