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
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
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
import java.math.BigInteger;
import java.util.ArrayList;
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

        Group parentGroup = dataTestUtils.createGroup("parentGroup");
        Group childGroup = dataTestUtils.createGroup("childGroup");

        parentGroup.setGroupRelationships(new HashSet<GroupRelationship>());
        childGroup.setGroupRelationships(new HashSet<GroupRelationship>());

        // This is not how it's supposed to be
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.PARENT));
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.CHILD));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.PARENT));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.CHILD));

        JoinRequest joinRequest = TestUtils.createJoinRequest(childGroup, JoinRequestStatus.COMPLETED);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,parentGroup,role));
        userRepository.save(user);

        PageRequest pageable = new PageRequest(0, 999);
        List<JoinRequest> joinRequests = joinRequestRepository.findByParentUser(user, pageable).getContent();

        Assert.assertTrue("The is one join request", !CollectionUtils.isEmpty(joinRequests));
    }

    /**
     * Test: Create a parent and child relationship. Add a join request to the child and a user to the parent
     * Fail: The join request is not found
     * @throws Exception
     */
    @Test
    public void testCountByParentUser() throws Exception {

        Group parentGroup = dataTestUtils.createGroup("parentGroup");
        Group childGroup = dataTestUtils.createGroup("childGroup");

        parentGroup.setGroupRelationships(new HashSet<GroupRelationship>());
        childGroup.setGroupRelationships(new HashSet<GroupRelationship>());

        // This is not how it's supposed to be
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.PARENT));
        parentGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.CHILD));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(childGroup, parentGroup, RelationshipTypes.PARENT));
        childGroup.getGroupRelationships().add(dataTestUtils.createGroupRelationship(parentGroup, childGroup, RelationshipTypes.CHILD));

        JoinRequest joinRequest = TestUtils.createJoinRequest(childGroup, JoinRequestStatus.SUBMITTED);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user, parentGroup, role));
        userRepository.save(user);

        BigInteger count = joinRequestRepository.countSubmittedByParentUser(user);

        Assert.assertTrue("The is one join request", count == BigInteger.ONE);
    }

    /**
     * Test: Create a parent and child relationship. Add a join request to the child and a user to the parent
     * Fail: The join request is not found
     * @throws Exception
     */
    @Test
    public void testCountByUser() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        JoinRequest joinRequest = TestUtils.createJoinRequest(group, JoinRequestStatus.SUBMITTED);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user, group, role));
        userRepository.save(user);

        BigInteger count = joinRequestRepository.countSubmittedByUser(user);

        Assert.assertTrue("The is one join request", count == BigInteger.ONE);
    }

    @Test
    public void testFindByUserAndGroups() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        JoinRequest joinRequest = TestUtils.createJoinRequest(group, JoinRequestStatus.SUBMITTED);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,group,role));
        userRepository.save(user);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        Page<JoinRequest> joinRequests = joinRequestRepository.findByUserAndGroups(user, groupIds,
                new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("There should be one join request", 1, joinRequests.getContent().size());
    }

    @Test
    public void testFindByUserAndGroupsAndStatuses() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        JoinRequest joinRequest = TestUtils.createJoinRequest(group, JoinRequestStatus.IGNORED);
        joinRequestRepository.save(joinRequest);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,group,role));
        userRepository.save(user);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        List<JoinRequestStatus> statuses = new ArrayList<>();
        statuses.add(JoinRequestStatus.IGNORED);

        Page<JoinRequest> joinRequests = joinRequestRepository.findByUserAndStatusesAndGroups(user, statuses, groupIds,
                new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("There should be one join request", 1, joinRequests.getContent().size());
    }
}