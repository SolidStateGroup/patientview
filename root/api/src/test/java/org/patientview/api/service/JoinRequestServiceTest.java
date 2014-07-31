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
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.test.util.TestUtils;

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

    @InjectMocks
    JoinRequestService joinRequestService = new JoinRequestServiceImpl();

    private User creator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser(1L, "creator");
    }


    /**
     * Test: Create a join request with attached units
     * Fail: The join request is created without error
     * @throws ResourceNotFoundException
     */
    @Test
    public void testAddJoinRequest() throws ResourceNotFoundException {

        Group group = TestUtils.createGroup(1L, "TestGroup", creator);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());


        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(joinRequest);

        joinRequest = joinRequestService.addJoinRequest(group.getId(), joinRequest);

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
    public void testAddJoinRequest_InValidGroup() throws ResourceNotFoundException {
        Group group = TestUtils.createGroup(1L, "TestGroup", creator);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setGroup(group);

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(null);

        joinRequestService.addJoinRequest(group.getId(), joinRequest);

        verify(groupRepository.findOne(any(Long.class)), Mockito.times(1));
        verify(joinRequestRepository.save(eq(joinRequest)), Mockito.times(0));
        Assert.fail("The service should throw an exception");
    }


}
