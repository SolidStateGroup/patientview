package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RequestTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.RequestRepository;
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
public class RequestRepositoryTest {

    @Inject
    DataTestUtils dataTestUtils;

    @Inject
    RequestRepository requestRepository;

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
     * Test: Create a parent and child relationship. Add a request to the child and a user to the parent
     * Fail: The request is not found
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

        Request request = TestUtils.createRequest(childGroup, RequestStatus.COMPLETED, RequestTypes.JOIN_REQUEST);
        requestRepository.save(request);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,parentGroup,role));
        userRepository.save(user);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        PageRequest pageable = PageRequest.of(0, 999);
        List<Request> requests = requestRepository.findByParentUser(user, requestTypes, pageable).getContent();

        Assert.assertTrue("The is one request", !CollectionUtils.isEmpty(requests));
    }

    /**
     * Test: Create a parent and child relationship. Add a request to the child and a user to the parent
     * Fail: The request is not found
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

        Request request = TestUtils.createRequest(childGroup, RequestStatus.SUBMITTED, RequestTypes.JOIN_REQUEST);
        requestRepository.save(request);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user, parentGroup, role));
        userRepository.save(user);

        BigInteger count = requestRepository.countSubmittedByParentUser(user.getId());

        Assert.assertTrue("The is one request", count == BigInteger.ONE);
    }

    /**
     * Test: Create a parent and child relationship. Add a request to the child and a user to the parent
     * Fail: The request is not found
     * @throws Exception
     */
    @Test
    public void testCountByUser() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        Request request = TestUtils.createRequest(group, RequestStatus.SUBMITTED, RequestTypes.JOIN_REQUEST);
        requestRepository.save(request);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user, group, role));
        userRepository.save(user);

        BigInteger count = requestRepository.countSubmittedByUser(user.getId());

        Assert.assertTrue("The is one request", count == BigInteger.ONE);
    }

    @Test
    public void testFindByUserAndGroups() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        Request request = TestUtils.createRequest(group, RequestStatus.SUBMITTED, RequestTypes.JOIN_REQUEST);
        requestRepository.save(request);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,group,role));
        userRepository.save(user);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        
        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        Page<Request> requests = requestRepository.findByUserAndGroups(user, groupIds, requestTypes,
                PageRequest.of(0, Integer.MAX_VALUE));

        Assert.assertEquals("There should be one request", 1, requests.getContent().size());
    }

    @Test
    public void testFindByUserAndGroupsAndStatuses() throws Exception {

        Group group = dataTestUtils.createGroup("parentGroup");

        Request request = TestUtils.createRequest(group, RequestStatus.IGNORED, RequestTypes.JOIN_REQUEST);
        requestRepository.save(request);

        User user = dataTestUtils.createUser("TestUser");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(dataTestUtils.createGroupRole(user,group,role));
        userRepository.save(user);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        List<RequestStatus> statuses = new ArrayList<>();
        statuses.add(RequestStatus.IGNORED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        Page<Request> requests = requestRepository.findByUserAndStatusesAndGroups(user, statuses, groupIds,
                requestTypes, PageRequest.of(0, Integer.MAX_VALUE));

        Assert.assertEquals("There should be one request", 1, requests.getContent().size());
    }
}