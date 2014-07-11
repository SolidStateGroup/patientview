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
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private EntityManager entityManager;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GroupService groupService = new GroupServiceImpl();

    private User creator;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser(1L, "creator");
    }


    /**
     * Test: To see if the parent and child from the GroupRelation object populate the transient objects
     *
     */
    @Test
    public void testFindAll(){

        // Set up groups
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);
        Group parentGroup = TestUtils.createGroup(2L, "parentGroup", creator);
        Group childGroup = TestUtils.createGroup(3L, "childGroup", creator);

        // Create relationships
        LookupType relationshipType = TestUtils.createLookupType(4L, "RELATIONSHIP_TYPE", creator);
        Lookup parentRelationship = TestUtils.createLookup(5L, relationshipType, "PARENT", creator);

        Lookup childRelationship = TestUtils.createLookup(6L, relationshipType, "PARENT", creator);

        Set<GroupRelationship> groupRelationships = new HashSet<GroupRelationship>();
        GroupRelationship parent =  TestUtils.createGroupRelationship(7L, testGroup, parentGroup, parentRelationship, creator);
        GroupRelationship child =  TestUtils.createGroupRelationship(8L, testGroup, childGroup, childRelationship, creator);
        groupRelationships.add(parent);
        groupRelationships.add(child);

        testGroup.setGroupRelationships(groupRelationships);

        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        when(groupRepository.findAll()).thenReturn(groups);
        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("CHILD"))).thenReturn(childRelationship);

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
        User testUser = TestUtils.createUser(2L, "testUser");
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);
        Group parentGroup = TestUtils.createGroup(5L, "parentGroup", creator);
        Group childGroup  = TestUtils.createGroup(6L, "childGroup", creator);
        Set<Group> childGroups = new HashSet<Group>();
        Set<Group> parentGroups = new HashSet<Group>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        // Create relationships lookups
        LookupType relationshipType = TestUtils.createLookupType(4L, "RELATIONSHIP_TYPE", creator);
        Lookup parentRelationship = TestUtils.createLookup(5L, relationshipType, "PARENT", creator);
        Lookup childRelationship = TestUtils.createLookup(6L, relationshipType, "CHILD", creator);

        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("CHILD"))).thenReturn(childRelationship);

        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Group group = groupService.create(testGroup);

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
    public void testAddGroupChildAndParentOnSave() {
        User testUser = TestUtils.createUser(2L, "testUser");
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);
        Group parentGroup = TestUtils.createGroup(5L, "parentGroup", creator);
        Group childGroup  = TestUtils.createGroup(6L, "childGroup", creator);
        Set<Group> childGroups = new HashSet<Group>();
        Set<Group> parentGroups = new HashSet<Group>();
        childGroups.add(childGroup);
        parentGroups.add(parentGroup);
        testGroup.setChildGroups(childGroups);
        testGroup.setParentGroups(parentGroups);

        // Create relationships loopkups
        LookupType relationshipType = TestUtils.createLookupType(4L, "RELATIONSHIP_TYPE", creator);
        Lookup parentRelationship = TestUtils.createLookup(5L, relationshipType, "PARENT", creator);
        Lookup childRelationship = TestUtils.createLookup(6L, relationshipType, "CHILD", creator);

        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(groupRepository.findOne(Matchers.eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupRepository.save(Matchers.eq(testGroup))).thenReturn(testGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        // Test
        Group group = groupService.save(testGroup);

        // Verify
        verify(groupRelationshipRepository, Mockito.times(1)).deleteBySourceGroup(Matchers.eq(testGroup));
        verify(groupRelationshipRepository, Mockito.times(4)).save(Matchers.any(GroupRelationship.class));
        Assert.assertNotNull("A group feature has been created", group);
    }

    /**
     * Test: To save group links
     *
     */
    @Test
    public void testGroupSaveWithLink() {
        User testUser = TestUtils.createUser(2L, "testUser");
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);

    }


    /**
     * Test: create parent and 2 children, get parent group and its children based on a user's membership of the parent group
     */
    @Test
    @Ignore
    // todo: finish checking list of groups against specialty admin
    public void testFindGroupAndChildGroupsByUser() {

        // create user
        User testUser = TestUtils.createUser(1L, "testUser");

        // create groups
        Group parentGroup = TestUtils.createGroup(2L, "parentGroup", creator);
        Group childGroup1  = TestUtils.createGroup(3L, "childGroup1", creator);
        Group childGroup2  = TestUtils.createGroup(4L, "childGroup2", creator);
        Set<Group> childGroups = new HashSet<Group>();
        childGroups.add(childGroup1);
        childGroups.add(childGroup2);
        parentGroup.setChildGroups(childGroups);
        List<Group> allGroups = new ArrayList<Group>();
        allGroups.add(parentGroup);

        // add user as specialty admin to group
        Role role = TestUtils.createRole(5L, "SPECIALTY_ADMIN", creator);
        GroupRole groupRole = TestUtils.createGroupRole(6L, role, parentGroup, testUser, creator);
        testUser.setGroupRoles(new HashSet<GroupRole>());
        testUser.getGroupRoles().add(groupRole);
        List<Role> roles = new ArrayList<Role>();
        roles.add(role);

        // Create relationships lookups
        LookupType relationshipType = TestUtils.createLookupType(5L, "RELATIONSHIP_TYPE", creator);
        Lookup parentRelationship = TestUtils.createLookup(6L, relationshipType, "PARENT", creator);
        Lookup childRelationship = TestUtils.createLookup(7L, relationshipType, "CHILD", creator);

        // create group relationships
        Set<GroupRelationship> groupRelationships = new HashSet<GroupRelationship>();
        GroupRelationship child1 =  TestUtils.createGroupRelationship(8L, parentGroup, childGroup1, childRelationship, creator);
        GroupRelationship child2 =  TestUtils.createGroupRelationship(9L, parentGroup, childGroup2, childRelationship, creator);
        groupRelationships.add(child1);
        groupRelationships.add(child2);
        parentGroup.setGroupRelationships(groupRelationships);

        // setup stubbing
        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.findByTypeAndValue(Matchers.anyString(), Matchers.eq("CHILD"))).thenReturn(childRelationship);
        when(userRepository.findOne(Matchers.eq(testUser.getId()))).thenReturn(testUser);
        when(roleRepository.findByUser(Matchers.eq(testUser))).thenReturn(roles);
        when(groupRepository.findOne(Matchers.eq(parentGroup.getId()))).thenReturn(parentGroup);
        when(groupRepository.findGroupByUser(Matchers.eq(testUser))).thenReturn(allGroups);
        when(groupRepository.save(Matchers.eq(parentGroup))).thenReturn(parentGroup);
        when(groupRelationshipRepository.save(Matchers.any(GroupRelationship.class))).thenReturn(new GroupRelationship());

        Group group = groupService.save(parentGroup);

        Assert.assertEquals("Should retrieve 3 groups", 3, securityService.getUserGroups(testUser.getId()).size());
    }
}
