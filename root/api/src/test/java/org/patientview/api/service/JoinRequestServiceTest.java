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

        Group specialty = TestUtils.createGroup(1L, "TestSpecialty", creator);
        Group unit =  TestUtils.createGroup(2L, "TestUnit", creator);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setSpecialty(specialty);
        joinRequest.setUnit(unit);

        when(groupRepository.findOne(eq(specialty.getId()))).thenReturn(specialty);
        when(groupRepository.findOne(eq(unit.getId()))).thenReturn(unit);
        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(joinRequest);

        joinRequest = joinRequestService.addJoinRequest(joinRequest);

        verify(groupRepository, Mockito.times(2)).findOne(any(Long.class));
        verify(joinRequestRepository, Mockito.times(1)).save(any(JoinRequest.class));

        Assert.assertNotNull("The return join request should not be null", joinRequest);
        Assert.assertNotNull("The specialty should not be null", joinRequest.getSpecialty());
        Assert.assertNotNull("The unit should not be null", joinRequest.getUnit());

    }

    /**
     * Test: Attempt to create a join request with an invalid specialty
     * Fail: The join request is created without error
     *
     * @throws ResourceNotFoundException
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testAddJoinRequest_InValidUnit() throws ResourceNotFoundException {
        Group specialty = TestUtils.createGroup(1L, "TestSpecialty", creator);
        Group unit =  TestUtils.createGroup(2L, "TestUnit", creator);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setSpecialty(specialty);
        joinRequest.setUnit(unit);

        when(groupRepository.findOne(eq(specialty.getId()))).thenReturn(null);
        when(groupRepository.findOne(eq(unit.getId()))).thenReturn(unit);

        joinRequestService.addJoinRequest(joinRequest);

        verify(groupRepository.findOne(any(Long.class)), Mockito.times(1));
        verify(joinRequestRepository.save(eq(joinRequest)), Mockito.times(0));
        Assert.fail("The service should throw an exception");
    }

    /**
     * Test: Attempt to create a join request with an invalid unit
     * Fail: The join request is created without error
     *
     * @throws ResourceNotFoundException
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testAddJoinRequest_InValidSpecialty() throws ResourceNotFoundException {
        Group specialty = TestUtils.createGroup(1L, "TestSpecialty", creator);
        Group unit =  TestUtils.createGroup(2L, "TestUnit", creator);

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setSpecialty(specialty);
        joinRequest.setUnit(unit);

        when(groupRepository.findOne(eq(specialty.getId()))).thenReturn(specialty);
        when(groupRepository.findOne(eq(unit.getId()))).thenReturn(null);

        joinRequestService.addJoinRequest(joinRequest);

        verify(groupRepository.findOne(any(Long.class)), Mockito.times(1));
        verify(joinRequestRepository.save(eq(joinRequest)), Mockito.times(0));
        Assert.fail("The service should throw an exception");
    }


}
