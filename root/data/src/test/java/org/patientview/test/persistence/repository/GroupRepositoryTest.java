package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class GroupRepositoryTest {

    @Inject
    GroupRepository groupRepository;

    @Inject
    DataTestUtils dataTestUtils;
    
    @Inject
    GroupFeatureRepository groupFeatureRepository;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    /**
     * Test: Assign a group with a role to a user and see if it's returned from the Query
     * Fail: No group is returned
     */
    @Test
    public void testFindGroupByUser() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);

        Iterable<Group> groups = groupRepository.findGroupByUser(user);

        Assert.assertTrue("There are no groups linked to the user", groups.iterator().hasNext());
    }

    @Test
    public void testFindGroupByFeature() {
        Group group = dataTestUtils.createGroup("testGroup");
        Feature feature = dataTestUtils.createFeature(FeatureType.MESSAGING.toString());
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setGroup(group);
        groupFeature.setFeature(feature);
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.getGroupFeatures().add(groupFeature);
        groupFeatureRepository.save(groupFeature);

        Iterable<Group> groups = groupRepository.findByFeature(feature);

        Assert.assertTrue("No group found by feature", groups.iterator().hasNext());
    }

    @Test
    public void testFindGroupByName() {
        Group group = dataTestUtils.createGroup("testGroup");
        Iterable<Group> groups = groupRepository.findByName(group.getName());
        Assert.assertTrue("There are no groups", groups.iterator().hasNext());
    }

    @Test
    public void testFindAllByIds() {
        Group group1 = dataTestUtils.createGroup("testGroup1");
        Group group2 = dataTestUtils.createGroup("testGroup2");

        List<Long> ids = new ArrayList<>();
        ids.add(group1.getId());
        ids.add(group2.getId());

        List<Group> groups = groupRepository.findAllByIds(ids);

        Assert.assertNotNull("There should be groups", groups);
        Assert.assertEquals("There should be 2 groups", 2, groups.size());
    }

    @Test
    public void findGroupAndChildGroupsByUser() {
        User user = dataTestUtils.createUser("testUser");
        Group parentGroup = dataTestUtils.createGroup("parentGroup");
        Role role = dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, parentGroup, role);
        Group childGroup = dataTestUtils.createGroup("childGroup");

        Lookup groupType1 = dataTestUtils.createLookup("SPECIALTY", LookupTypes.GROUP);
        parentGroup.setGroupType(groupType1);
        Lookup groupType2 = dataTestUtils.createLookup("UNIT", LookupTypes.GROUP);
        childGroup.setGroupType(groupType2);

        String[] groupTypes = new String[]{groupType1.getId().toString(), groupType2.getId().toString()};

        parentGroup.setGroupRelationships(new HashSet<GroupRelationship>());
        childGroup.setGroupRelationships(new HashSet<GroupRelationship>());

        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.PARENT));
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.CHILD));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.PARENT));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.CHILD));

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        //Page<Group> groupPage = groupRepository.findGroupAndChildGroupsByUserAndGroupType("%%", convertStringArrayToLongs(groupTypes), user, pageable);
        Page<Group> groupPage = groupRepository.findGroupAndChildGroupsByUser("%%", user, pageable);

        assertTrue("Parent group should have children", !CollectionUtils.isEmpty(groupRepository.findChildren(parentGroup)));
        assertTrue("User should be linked to parent group", !CollectionUtils.isEmpty(TestUtils.iterableToList(groupRepository.findGroupByUser(user))));

        Group testParentGroup = TestUtils.iterableToList(groupRepository.findGroupByUser(user)).get(0);
        boolean flag = false;
        for (GroupRelationship groupRelationship : testParentGroup.getGroupRelationships()) {
            if (groupRelationship.getSourceGroup().equals(childGroup)) {
                flag = true;
            }
        }
        assertTrue("The child should be linked to the parent", flag);
        assertTrue("Child group should exist", groupPage.getContent().contains(childGroup));
    }

    @Test
    public void testGroupContactPoints() {
        Group group = dataTestUtils.createGroup("testGroup");
        Lookup lookup = dataTestUtils.createLookup("SPECIALTY", LookupTypes.GROUP);
        group.setGroupType(lookup);
        group.setContactPoints(new HashSet<ContactPoint>());

        LookupType lookupType = new LookupType();
        lookupType.setType(LookupTypes.CONTACT_POINT_TYPE);

        ContactPointType contactPointType = new ContactPointType();
        contactPointType.setLookupType(lookupType);
        contactPointType.setValue(ContactPointTypes.PV_ADMIN_NAME);

        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setCreator(creator);
        contactPoint.setContactPointType(contactPointType);
        contactPoint.setContent("Dr PV Admin");

        group.getContactPoints().add(contactPoint);

        Group entityGroup = groupRepository.save(group);
        Iterator iter = entityGroup.getContactPoints().iterator();

        Assert.assertTrue("Group should have contact points", iter.hasNext());

        ContactPoint firstContactPoint = (ContactPoint) iter.next();

        Assert.assertTrue("Contact point type should be PV_ADMIN_NAME",
                firstContactPoint.getContactPointType().getValue().equals(ContactPointTypes.PV_ADMIN_NAME));
    }

    private List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        for (String string : strings) {
            longs.add(Long.parseLong(string));
        }
        return longs;
    }
}
