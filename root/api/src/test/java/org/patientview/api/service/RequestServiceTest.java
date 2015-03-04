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
import org.patientview.api.service.impl.RequestServiceImpl;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RequestRepository;
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
public class RequestServiceTest {

    @Mock
    RequestRepository requestRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;

    @Mock
    CaptchaService captchaService;

    @Mock
    Properties properties;

    @InjectMocks
    RequestService requestService = new RequestServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: Create a request with attached units
     * Fail: The request is created without error
     * @throws ResourceNotFoundException
     */
    @Test
    public void testAddRequest() throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {

        Group group = TestUtils.createGroup("TestGroup");
        group.setContactPoints(new HashSet<ContactPoint>());
        group.getContactPoints().add(TestUtils.createContactPoint("123", ContactPointTypes.PV_ADMIN_EMAIL));

        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroupId(group.getId());

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        request = requestService.add(request);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        verify(requestRepository, Mockito.times(1)).save(any(Request.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));

        Assert.assertNotNull("The return request should not be null", request);
        Assert.assertNotNull("The group should not be null", request.getGroup());
    }

    /**
     * Test: Attempt to create a request with an invalid specialty
     * Fail: The request is created without error
     *
     * @throws ResourceNotFoundException
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testAddRequest_invalidGroup() throws ResourceNotFoundException, ResourceForbiddenException {
        Group group = TestUtils.createGroup("TestGroup");

        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroup(group);
        request.setGroupId(group.getId());

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(null);

        requestService.add(request);

        verify(groupRepository, Mockito.times(1)).findOne(any(Long.class));
        Assert.fail("The service should throw an exception");
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetRequestUnitAdmin_validGroup() throws ResourceNotFoundException, ResourceForbiddenException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroup(group);

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        when(requestRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(requestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {RequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        requestService.getByUser(group.getId(), new GetParameters());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(requestRepository, Mockito.times(1)).findByUser(eq(user), eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    private List<RequestStatus> convertStringArrayToStatusList (String[] statuses) {
        List<RequestStatus> statusList = new ArrayList<>();
        for (String status : statuses) {
            RequestStatus found = RequestStatus.valueOf(status);
            if (found != null) {
                statusList.add(found);
            }
        }
        return statusList;
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetRequestUnitAdminByStatus_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroup(group);

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        when(requestRepository.findByUserAndStatuses(eq(user), any(new ArrayList<Request>().getClass()),
                any(Pageable.class))).thenReturn(requestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {RequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        requestService.getByUser(group.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(requestRepository, Mockito.times(1)).findByUserAndStatuses(eq(user),
            eq(convertStringArrayToStatusList(getParameters.getStatuses())), eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetRequestSpecialtyAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user, RoleName.SPECIALTY_ADMIN);

        Group group = TestUtils.createGroup( "TestGroup");
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroup(group);

        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        when(requestRepository.findByParentUser(eq(user), any(Pageable.class))).thenReturn(requestPage);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(user);

        requestService.getByUser(group.getId(), new GetParameters());

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(requestRepository, Mockito.times(1)).findByParentUser(eq(user),
                eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: An exception should be thrown for the null group
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetRequest_invalidGroup() throws ResourceNotFoundException {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("TestGroup");

        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroup(group);

        when(userRepository.findOne(eq(group.getId()))).thenReturn(null);

        GetParameters getParameters = new GetParameters();
        String[] statuses = {RequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        requestService.getByUser(group.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(requestRepository, Mockito.times(0)).findByUser(eq(user), eq(new PageRequest(0, Integer.MAX_VALUE)));
    }

    /**
     * Test: Save a request from the controller
     * Fail: A called to the save method should be called
     */
    @Test
    public void testSaveRequest() throws ResourceNotFoundException, SecurityException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        Request request = new Request();
        request.setId(10L);
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setStatus(RequestStatus.SUBMITTED);

        when(requestRepository.findOne(eq(request.getId()))).thenReturn(request);
        when(requestRepository.save(eq(request))).thenReturn(request);

        requestService.save(request);

        verify(requestRepository, Mockito.times(1)).save(eq(request));
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetRequestCountUnitAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser( "testUser");
        user.setId(1L);
        TestUtils.authenticateTest(user, RoleName.UNIT_ADMIN);

        Group group = TestUtils.createGroup( "TestGroup");
        when(userRepository.exists(eq(user.getId()))).thenReturn(true);

        requestService.getCount(user.getId());

        verify(requestRepository, Mockito.times(1)).countSubmittedByUser(eq(user.getId()));
    }

    /**
     * Test: Attempt to retrieve the request that are related to a user
     * Fail: Appropriate service method not called
     */
    @Test
    public void testGetRequestCountSpecialtyAdmin_validGroup() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        TestUtils.authenticateTest(user, RoleName.SPECIALTY_ADMIN);

        Group group = TestUtils.createGroup("TestGroup");
        when(userRepository.exists(eq(user.getId()))).thenReturn(true);

        requestService.getCount(user.getId());

        verify(requestRepository, Mockito.times(1)).countSubmittedByParentUser(eq(user.getId()));
    }
}
