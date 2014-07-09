package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private EntityManager entityManager;

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
        when(lookupRepository.getByLookupTypeAndValue(Matchers.anyString(), Matchers.eq("PARENT"))).thenReturn(parentRelationship);
        when(lookupRepository.getByLookupTypeAndValue(Matchers.anyString(), Matchers.eq("CHILD"))).thenReturn(childRelationship);

        ((GroupServiceImpl) groupService).init();

        groups = groupService.findAll();

        Assert.assertFalse("There should be parent objects", CollectionUtils.isEmpty(groups.get(0).getParentGroups()));
        Assert.assertFalse("There should be child objects", CollectionUtils.isEmpty(groups.get(0).getChildGroups()));

        //Assert.assertTrue("There should be the correct child objects", groups.get(0).getParentGroups().getId().equals(childGroup.getId()));
        //Assert.assertTrue("There should be the correct parent objects", groups.get(0).getId().equals(parentGroup.getId()));
    }


}
