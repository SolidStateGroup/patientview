package org.patientview.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
public class JoinRequestControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private JoinRequestService joinRequestService;

    @InjectMocks
    private JoinRequestController joinRequestController;

    private MockMvc mockMvc;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(joinRequestController).build();
    }

    /**
     * Test: The request of get all of the join request statuses
     * Fail: The service does not get called for join requests
     */
    @Test
    public void testGroupJoinRequest_statuses() throws ResourceNotFoundException, JsonProcessingException {
        String url = "/joinrequest/statuses";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));

        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

    }


    /**
     * Test: The request of the join request for a unit
     * Fail: The service does not get called for join requests
     */
    @Test
    public void testGroupJoinRequest() throws ResourceNotFoundException {
        Long groupId = 1L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + groupId + "/joinrequests"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(joinRequestService, Mockito.times(1)).get(eq(groupId));
    }

    /**
     * Test: The request of the join request for a unit
     * Fail: The service does not get called for join requests
     */
    @Test
    public void testGroupJoinRequest_withParameter() throws ResourceNotFoundException, JsonProcessingException{
        Long userId = 1L;

        Set<JoinRequestStatus> statuses = new HashSet<>();

        String url = "/user/" + userId + "/joinrequests?statuses=" + mapper.writeValueAsString(statuses);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(joinRequestService, Mockito.times(1)).getByType(eq(userId), eq(Collections.EMPTY_SET));
    }

    /**
     * Test: The request of the join request for a unit
     * Fail: The service does not get called for join requests
     */
    @Test
    public void testGroupJoinRequest_withParameterAndData() throws ResourceNotFoundException, JsonProcessingException{
        Long userId = 1L;

        Set<String> statuses = new HashSet<>();
        statuses.add(JoinRequestStatus.COMPLETED.getId());

        Set<JoinRequestStatus> returnStatuses = new HashSet<>();
        returnStatuses.add(JoinRequestStatus.COMPLETED);

        String url = "/user/" + userId + "/joinrequests?statuses=" + mapper.writeValueAsString(statuses);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(joinRequestService, Mockito.times(1)).getByType(eq(userId), eq(returnStatuses));
    }

    /**
     * Test: Saving a join request from the admin screen
     * Fail: The save method is not found
     */
    @Test
    public void testJoinRequestSave() throws ResourceNotFoundException {

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);
        joinRequest.setSurname("Test");
        joinRequest.setForename("James");
        joinRequest.setNhsNumber("324234");
        joinRequest.setDateOfBirth(new Date());

        String url = "/joinrequest";

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(joinRequestService, Mockito.times(1)).save(eq(joinRequest));

    }


}
