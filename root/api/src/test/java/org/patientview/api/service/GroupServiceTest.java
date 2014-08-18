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
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.LookupTypes;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
     * Test: To see if the parent and child from the GroupRelation object populate the transient objects
     *
     */
    @Test
    public void testFindAll(){

        // Set up groups
        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup = TestUtils.createGroup("childGroup");

        // Create relationships
        LookupType relationshipType = TestUtils.createLookupType(LookupTypes.RELATIONSHIP_TYPE);
        Lookup parentRelationship = TestUtils.createLookup(relationshipType, "PARENT");

        Lookup childRelationship = TestUtils.createLookup(relationshipType, "PARENT");

        Set<GroupRelationship> groupRelationships = new HashSet<GroupRelationship>();
        GroupRelationship parent =  TestUtils.createGroupRelationship(testGroup, parentGroup, RelationshipTypes.PARENT);
        GroupRelationship child =  TestUtils.createGroupRelationship(testGroup, childGroup, RelationshipTypes.CHILD);
        groupRelationships.add(parent);
        groupRelationships.add(child);

        testGroup.setGroupRelationships(groupRelationships);

        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        when(groupRepository.findAll()).thenReturn(groups);
        when(lookupRepository.findByTypeAndValue(Matchers.eq(LookupTypes.RELATIONSHIP_TYPE), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.findByTypeAndValue(Matchers.eq(LookupTypes.RELATIONSHIP_TYPE), Matchers.eq("CHILD"))).thenReturn(childRelationship);

        groups = groupService.findAll();

        Assert.assertFalse("There should be parent objects", CollectionUtils.isEmpty(groups.get(0).getParentGroups()));
        Assert.assertFalse("There should be child objects", CollectionUtils.isEmpty(groups.get(0).getChildGroups()));

        //Assert.assertTrue("There should be the correct child objects", groups.get(0).getParentGroups().getId().equals(childGroup.getId()));
        //Assert.assertTrue("There should be the correct parent objects", groups.get(0).getId().equals(parentGroup.getId()));
    }


    /**
     * Test: The creation of the parent and child groups
     * Fail: The the parent and child groups are not returned
     *
     */
    @Test

    public void testAddGroupChildAndParent() {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser, Collections.EMPTY_LIST);

        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup  = TestUtils.createGroup("childGroup");
        Set<Group> childGroups = new HashSet<Group>();
        Set<Group> parentGroups = new HashSet<Group>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        // Create relationships lookups
        LookupType relationshipType = TestUtils.createLookupType(LookupTypes.RELATIONSHIP_TYPE);
        Lookup parentRelationship = TestUtils.createLookup(relationshipType, "PARENT");
        Lookup childRelationship = TestUtils.createLookup(relationshipType, "CHILD");

        when(lookupRepository.findByTypeAndValue(Matchers.eq(LookupTypes.RELATIONSHIP_TYPE), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.findByTypeAndValue(Matchers.eq(LookupTypes.RELATIONSHIP_TYPE), Matchers.eq("CHILD"))).thenReturn(childRelationship);

        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
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
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Group parentGroup = TestUtils.createGroup("parentGroup");
        Group childGroup  = TestUtils.createGroup("childGroup");
        Set<Group> childGroups = new HashSet<Group>();
        Set<Group> parentGroups = new HashSet<Group>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        TestUtils.authenticateTest(testUser, Collections.EMPTY_LIST);
        Group group = groupService.add(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group feature has been created", group);
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

        groupRole = groupService.addGroupRole(testUser.getId(), testGroup.getId(), testRole.getId());

        Assert.assertNotNull("The returned object should not be null", groupRole);

        verify(groupRoleRepository, Mockito.times(1)).save(Matchers.any(GroupRole.class));


    }

    @Test
    public void testAddGroupFeature() {
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature testFeature = TestUtils.createFeature(FeatureType.MESSAGING.getName());
        GroupFeature groupFeature = TestUtils.createGroupFeature(testFeature, testGroup);

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
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature testFeature = TestUtils.createFeature(FeatureType.MESSAGING.getName());
        GroupFeature groupFeature = TestUtils.createGroupFeature(testFeature, testGroup);

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
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Group testParentGroup = TestUtils.createGroup("testGroup");

        when(userRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.findOne(Matchers.eq(testParentGroup.getId()))).thenReturn(testParentGroup);

        groupService.addParentGroup(testGroup.getId(), testParentGroup.getId());

        // Parent and child relationship should be persist
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
        Set<Group> childGroups = new HashSet<Group>();
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
            Assert.assertEquals("Should retrieve 3 groups", 3, securityService.getUserGroups(testUser.getId()).size());
        } catch (ResourceNotFoundException rnf) {
            return;
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
}
