package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class JoinRequestRepositoryTest {

    @Inject
    DataTestUtils dataTestUtils;

    @Inject
    JoinRequestRepository joinRequestRepository;

    @Inject
    UserRepository userRepository;

    User creator;


    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    public void testFindByUser() throws Exception {

    }

    public void testFindByUserAndType() throws Exception {

    }

    /**
     * Test: Create a parent and child relationship. Add a join request to the child and a user to the parent
     * Fail: The join request is not found
     * @throws Exception
     */
    @Test
    public void testFindByParentUser() throws Exception {

        Group parentGroup = dataTestUtils.createGroup("parentGroup", creator);
        Group childGroup = dataTestUtils.createGroup("childGroup", creator);

        parentGroup.setGroupRelationships(new HashSet<GroupRelationship>());
        childGroup.setGroupRelationships(new HashSet<GroupRelationship>());

        // This is not how it's supposed to be
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.PARENT, creator));
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.CHILD, creator));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.PARENT, creator));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.CHILD, creator));

        JoinRequest joinRequest = TestUtils.createJoinRequest(childGroup);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole("TestRole", creator);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,parentGroup,role, creator));
        userRepository.save(user);

        List<JoinRequest> joinRequests = TestUtils.iterableToList(joinRequestRepository.findByParentUser(user));

        Assert.assertTrue("The is one join request", !CollectionUtils.isEmpty(joinRequests));

    }
}