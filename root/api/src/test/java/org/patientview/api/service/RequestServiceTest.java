package org.patientview.api.service;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.RequestServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RequestTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
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
import java.util.Optional;
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
    CaptchaService captchaService;

    @Mock
    EmailService emailService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    Properties properties;

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestService requestService = new RequestServiceImpl();

    @Mock
    UserRepository userRepository;

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
        request.setType(RequestTypes.JOIN_REQUEST);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(captchaService.verify(any(String.class))).thenReturn(true);
        request = requestService.add(request);

        verify(groupRepository, Mockito.times(1)).findById(any(Long.class));
        verify(requestRepository, Mockito.times(1)).save(any(Request.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));

        Assert.assertNotNull("The return request should not be null", request);
        Assert.assertNotNull("The group should not be null", request.getGroup());
    }

    @Test
    public void testAddGPRequest() throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {

        Group group = TestUtils.createGroup("GeneralPractice");
        group.setId(8L);// need 8 for GP group

        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setGroupId(group.getId());
        request.setType(RequestTypes.JOIN_REQUEST);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(captchaService.verify(any(String.class))).thenReturn(true);

        request = requestService.add(request);

        verify(groupRepository, Mockito.times(1)).findById(any(Long.class));
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

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.empty());
        when(captchaService.verify(any(String.class))).thenReturn(true);

        requestService.add(request);

        verify(groupRepository, Mockito.times(1)).findById(any(Long.class));
        Assert.fail("The service should throw an exception");
    }

    /**
     * Test: Complete a SUBMITTED forgot login request where the user has now logged in successfully
     * @throws ResourceNotFoundException
     * @throws MessagingException
     * @throws ResourceForbiddenException
     */
    @Test
    public void testCompleteForgotLoginRequest()
            throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        // store "old" request
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setType(RequestTypes.FORGOT_LOGIN);
        request.setCreated(new DateTime(new Date()).minusMonths(1).toDate());
        request.setNhsNumber("111 111 1111");

        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestsPage = new PageImpl<>(requests);

        // patient
        User patient = TestUtils.createUser("patient");
        patient.setCreated(new Date());
        patient.setLastLogin(new Date());

        // lookup
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // identifier
        String identifierString = "1111111111";
        Identifier identifier = TestUtils.createIdentifier(lookup, patient, identifierString);
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // request types and status to search for
        List<RequestStatus> submittedStatus = new ArrayList<>();
        submittedStatus.add(RequestStatus.SUBMITTED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);
        requestTypes.add(RequestTypes.FORGOT_LOGIN);

        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class)))
                .thenReturn(requestsPage);
        when(identifierRepository.findByValue(identifierString)).thenReturn(identifiers);

        // complete requests (should only complete one)
        int count = requestService.completeRequests();

        verify(requestRepository, Mockito.times(1))
                .findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class));
        verify(requestRepository, Mockito.times(1)).save(any(Request.class));
        Assert.assertEquals("Should have returned count of 1", 1, count);
    }

    /**
     * Test: Do not complete a SUBMITTED forgot login request as the user has not logged in successfully
     * @throws ResourceNotFoundException
     * @throws MessagingException
     * @throws ResourceForbiddenException
     */
    @Test
    public void testCompleteForgotLoginRequest_notLoggedIn()
            throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        // store "old" request
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setType(RequestTypes.FORGOT_LOGIN);
        request.setCreated(new DateTime(new Date()).minusMonths(1).toDate());
        request.setNhsNumber("111 111 1111");

        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestsPage = new PageImpl<>(requests);

        // patient
        User patient = TestUtils.createUser("patient");
        patient.setCreated(new Date());
        patient.setLastLogin(new DateTime(new Date()).minusMonths(2).toDate());

        // lookup
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // identifier
        String identifierString = "1111111111";
        Identifier identifier = TestUtils.createIdentifier(lookup, patient, identifierString);
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // request types and status to search for
        List<RequestStatus> submittedStatus = new ArrayList<>();
        submittedStatus.add(RequestStatus.SUBMITTED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);
        requestTypes.add(RequestTypes.FORGOT_LOGIN);

        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class)))
                .thenReturn(requestsPage);
        when(identifierRepository.findByValue(identifierString)).thenReturn(identifiers);

        // complete requests (should not complete any)
        int count = requestService.completeRequests();

        verify(requestRepository, Mockito.times(1))
                .findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class));
        verify(requestRepository, Mockito.times(0)).save(any(Request.class));
        Assert.assertEquals("Should have returned count of 0", 0, count);
    }

    /**
     * Test: Complete a SUBMITTED join request where the user has now been created
     * @throws ResourceNotFoundException
     * @throws MessagingException
     * @throws ResourceForbiddenException
     */
    @Test
    public void testCompleteJoinRequest()
            throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        // store "old" request
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setType(RequestTypes.JOIN_REQUEST);
        request.setCreated(new DateTime(new Date()).minusMonths(1).toDate());
        request.setNhsNumber("111 111 1111");

        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestsPage = new PageImpl<>(requests);

        // patient
        User patient = TestUtils.createUser("patient");
        patient.setCreated(new Date());

        // lookup
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // identifier
        String identifierString = "1111111111";
        Identifier identifier = TestUtils.createIdentifier(lookup, patient, identifierString);
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // request types and status to search for
        List<RequestStatus> submittedStatus = new ArrayList<>();
        submittedStatus.add(RequestStatus.SUBMITTED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);
        requestTypes.add(RequestTypes.FORGOT_LOGIN);

        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class)))
                .thenReturn(requestsPage);
        when(identifierRepository.findByValue(identifierString)).thenReturn(identifiers);

        // complete requests (should only complete one)
        int count = requestService.completeRequests();

        verify(requestRepository, Mockito.times(1))
                .findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class));
        verify(requestRepository, Mockito.times(1)).save(any(Request.class));
        Assert.assertEquals("Should have returned count of 1", 1, count);
    }

    /**
     * Test: Do not complete a SUBMITTED join request as the user was created before the join request
     * @throws ResourceNotFoundException
     * @throws MessagingException
     * @throws ResourceForbiddenException
     */
    @Test
    public void testCompleteJoinRequest_userForgot()
            throws ResourceNotFoundException, MessagingException, ResourceForbiddenException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        // store "new" request
        Request request = new Request();
        request.setForename("Test");
        request.setSurname("User");
        request.setDateOfBirth(new Date());
        request.setType(RequestTypes.JOIN_REQUEST);
        request.setCreated(new Date());
        request.setNhsNumber("111 111 1111");

        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestsPage = new PageImpl<>(requests);

        // patient
        User patient = TestUtils.createUser("patient");
        patient.setCreated(new DateTime(new Date()).minusMonths(1).toDate());

        // lookup
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        // identifier
        String identifierString = "1111111111";
        Identifier identifier = TestUtils.createIdentifier(lookup, patient, identifierString);
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // request types and status to search for
        List<RequestStatus> submittedStatus = new ArrayList<>();
        submittedStatus.add(RequestStatus.SUBMITTED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);
        requestTypes.add(RequestTypes.FORGOT_LOGIN);

        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(requestRepository.findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class)))
                .thenReturn(requestsPage);
        when(identifierRepository.findByValue(identifierString)).thenReturn(identifiers);

        // complete requests (shouldn't complete any)
        int count = requestService.completeRequests();

        verify(requestRepository, Mockito.times(1))
                .findAllByStatuses(eq(submittedStatus), eq(requestTypes), any(Pageable.class));
        verify(requestRepository, Mockito.times(0)).save(any(Request.class));
        Assert.assertEquals("Should have returned count of 1", 0, count);
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

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        Pageable pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        
        when(requestRepository.findByUser(eq(user), eq(requestTypes), any(Pageable.class))).thenReturn(requestPage);
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));

        GetParameters getParameters = new GetParameters();
        getParameters.setTypes(new String[]{RequestTypes.JOIN_REQUEST.toString()});

        requestService.getByUser(user.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findById(eq(user.getId()));
        verify(requestRepository, Mockito.times(1)).findByUser(eq(user), eq(requestTypes),
                eq(PageRequest.of(0, Integer.MAX_VALUE)));
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

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        Pageable pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        when(requestRepository.findByUserAndStatuses(eq(user),
                any(new ArrayList<Request>().getClass()),
                eq(requestTypes), any(Pageable.class))).thenReturn(requestPage);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        GetParameters getParameters = new GetParameters();
        getParameters.setStatuses(new String[]{RequestStatus.COMPLETED.toString()});
        getParameters.setTypes(new String[]{RequestTypes.JOIN_REQUEST.toString()});

        requestService.getByUser(user.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findById(eq(user.getId()));
        verify(requestRepository, Mockito.times(1)).findByUserAndStatuses(eq(user),
            eq(convertStringArrayToStatusList(getParameters.getStatuses())), eq(requestTypes), 
                eq(PageRequest.of(0, Integer.MAX_VALUE)));
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

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        Pageable pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        Page<Request> requestPage = new PageImpl<>(requests, pageableAll, requests.size());
        
        when(requestRepository.findByParentUser(eq(user), eq(requestTypes),
                any(Pageable.class))).thenReturn(requestPage);
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        
        GetParameters getParameters = new GetParameters();
        getParameters.setTypes(new String[]{RequestTypes.JOIN_REQUEST.toString()});

        requestService.getByUser(user.getId(), getParameters);

        verify(requestRepository, Mockito.times(1)).findByParentUser(eq(user),
                eq(requestTypes), eq(PageRequest.of(0, Integer.MAX_VALUE)));
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

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.empty());

        GetParameters getParameters = new GetParameters();
        String[] statuses = {RequestStatus.COMPLETED.toString()};
        getParameters.setStatuses(statuses);

        requestService.getByUser(user.getId(), getParameters);

        verify(userRepository, Mockito.times(1)).findById(eq(user.getId()));
        verify(requestRepository, Mockito.times(0)).findByUser(eq(user), eq(requestTypes), 
                eq(PageRequest.of(0, Integer.MAX_VALUE)));
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

        when(requestRepository.findById(eq(request.getId()))).thenReturn(Optional.of(request));
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
        when(userRepository.existsById(eq(user.getId()))).thenReturn(true);

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
        when(userRepository.existsById(eq(user.getId()))).thenReturn(true);

        requestService.getCount(user.getId());

        verify(requestRepository, Mockito.times(1)).countSubmittedByParentUser(eq(user.getId()));
    }
}
