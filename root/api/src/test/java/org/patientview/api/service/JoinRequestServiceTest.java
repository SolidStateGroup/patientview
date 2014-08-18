package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.JoinRequestServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.Collections;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
public class JoinRequestServiceTest {

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JoinRequestService joinRequestService = new JoinRequestServiceImpl();

    private User creator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }


    /**
     * Test: Create a join request with attached units
     * Fail: The join request is created without error
     * @throws ResourceNotFoundException
     */
    @Test
    public void testAddJoinRequest() throws ResourceNotFoundException {

        Group group = TestUtils.createGroup("TestGroup");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(joinRequest);

        joinRequest = joinRequestService.add(group.getId(), joinRequest);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        verify(joinRequestRepository, Mockito.times(1)).save(any(JoinRequest.class));

        Assert.assertNotNull("The return join request should not be null", joinRequest);
        Assert.assertNotNull("The group should not be null", joinRequest.getGroup());

    }

    /**
     * Test: Attempt to create a join request with an invalid specialty
     * Fail: The join request is created without error
     *
     * @throws ResourceNotFoundException
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testAddJoinRequest_invalidGroup() throws ResourceNotFoundException {
        Group group = TestUtils.createGroup("TestGroup");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(null);

        joinRequestService.add(group.getId(), joinRequest);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        Assert.fail("The service should throw an exception");
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestUnitAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.get(group.getId());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByUser(eq(user));
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestUnitAdminByStatus_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.getByStatus(group.getId(), JoinRequestStatus.COMPLETED);

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByUserAndStatus(eq(user), eq(JoinRequestStatus.COMPLETED));
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestSpecialtyAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.SPECIALTY_ADMIN);

        Group group = TestUtils.createGroup( "TestGroup");
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.get(group.getId());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByParentUser(eq(user));
    }


    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: An exception should be thrown for the null group
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetJoinRequest_invalidGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("TestGroup");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(null);

        joinRequestService.get(group.getId());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(0)).findByUser(eq(user));
    }


    /**
     * Test: Save a join request from the controller
     * Fail: A called to the save method should be called
     */
    @Test
    public void testSaveJoinRequest() throws ResourceNotFoundException, SecurityException {
        User user = TestUtils.createUser("testUser");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(10L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);

        TestUtils.authenticateTest(user, Collections.EMPTY_LIST);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(joinRequestRepository.findOne(eq(joinRequest.getId()))).thenReturn(joinRequest);
        joinRequestService.save(joinRequest);

        verify(joinRequestRepository, Mockito.times(1)).save(eq(joinRequest));

    }


    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestCountUnitAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser( "testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup( "TestGroup");
        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.getCount(group.getId());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).countSubmittedByUser(eq(user));
    }



    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestCountSpecialtyAdmin_validGroup() throws ResourceNotFoundException {


        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.SPECIALTY_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.getCount(group.getId());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).countSubmittedByParentUser(eq(user));
    }
}
