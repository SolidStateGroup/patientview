package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.Email;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.JoinRequestServiceImpl;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

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

    @Mock
    EmailService emailService;

    @Mock
    Properties properties;

    @InjectMocks
    JoinRequestService joinRequestService = new JoinRequestServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: Create a join request with attached units
     * Fail: The join request is created without error
     * @throws ResourceNotFoundException
     */
    @Test
    public void testAddJoinRequest() throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {

        Group group = TestUtils.createGroup("TestGroup");
        group.setContactPoints(new HashSet<ContactPoint>());
        group.getContactPoints().add(TestUtils.createContactPoint("123", ContactPointTypes.PV_ADMIN_EMAIL));

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroupId(group.getId());

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(joinRequest);

        joinRequest = joinRequestService.add(joinRequest);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        verify(joinRequestRepository, Mockito.times(1)).save(any(JoinRequest.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));

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
    public void testAddJoinRequest_invalidGroup() throws ResourceNotFoundException, ResourceForbiddenException {
        Group group = TestUtils.createGroup("TestGroup");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);
        joinRequest.setGroupId(group.getId());

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(null);

        joinRequestService.add(joinRequest);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        Assert.fail("The service should throw an exception");
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestUnitAdmin_validGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<JoinRequest> joinRequests = new ArrayList<>();
        joinRequests.add(joinRequest);
        Page<JoinRequest> joinRequestPage = new PageImpl<>(joinRequests, pageableAll, joinRequests.size());
        when(joinRequestRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(joinRequestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {JoinRequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        joinRequestService.getByUser(group.getId(), new GetParameters());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByUser(eq(user), eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    private List<JoinRequestStatus> convertStringArrayToStatusList (String[] statuses) {
        List<JoinRequestStatus> statusList = new ArrayList<>();
        for (String status : statuses) {
            JoinRequestStatus found = JoinRequestStatus.valueOf(status);
            if (found != null) {
                statusList.add(found);
            }
        }
        return statusList;
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

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<JoinRequest> joinRequests = new ArrayList<>();
        joinRequests.add(joinRequest);
        Page<JoinRequest> joinRequestPage = new PageImpl<>(joinRequests, pageableAll, joinRequests.size());
        when(joinRequestRepository.findByUserAndStatuses(eq(user), any(new ArrayList<JoinRequest>().getClass()),
                any(Pageable.class))).thenReturn(joinRequestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {JoinRequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        joinRequestService.getByUser(group.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByUserAndStatuses(eq(user),
            eq(convertStringArrayToStatusList(getParameters.getStatuses())), eq(new PageRequest(0, Integer.MAX_VALUE)));
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

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<JoinRequest> joinRequests = new ArrayList<>();
        joinRequests.add(joinRequest);
        Page<JoinRequest> joinRequestPage = new PageImpl<>(joinRequests, pageableAll, joinRequests.size());
        when(joinRequestRepository.findByParentUser(eq(user), any(Pageable.class))).thenReturn(joinRequestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        joinRequestService.getByUser(group.getId(), new GetParameters());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(1)).findByParentUser(eq(user),
                eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: An exception should be thrown for the null group
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetJoinRequest_invalidGroup() throws ResourceNotFoundException {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("TestGroup");

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(null);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {JoinRequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        joinRequestService.getByUser(group.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(joinRequestRepository, Mockito.times(0)).findByUser(eq(user), eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    /**
     * Test: Save a join request from the controller
     * Fail: A called to the save method should be called
     */
    @Test
    public void testSaveJoinRequest() throws ResourceNotFoundException, SecurityException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(10L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);

        when(joinRequestRepository.findOne(eq(joinRequest.getId()))).thenReturn(joinRequest);
        when(joinRequestRepository.save(eq(joinRequest))).thenReturn(joinRequest);

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
        user.setId(1L);
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup( "TestGroup");
        when(userRepository.exists(eq(user.getId()))).thenReturn(true);

        joinRequestService.getCount(user.getId());

        verify(joinRequestRepository, Mockito.times(1)).countSubmittedByUser(eq(user.getId()));
    }

    /**
     * Test: Attempt to retrieve the join request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetJoinRequestCountSpecialtyAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        TestUtils.authenticateTest(user, RoleName.SPECIALTY_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        when(userRepository.exists(eq(user.getId()))).thenReturn(true);

        joinRequestService.getCount(user.getId());

        verify(joinRequestRepository, Mockito.times(1)).countSubmittedByParentUser(eq(user.getId()));
    }
}
