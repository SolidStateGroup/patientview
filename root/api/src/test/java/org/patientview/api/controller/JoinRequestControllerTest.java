package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
     * Test: The submission of a JoinRequest object to the controller
     * Fail: The JoinRequest object is not passed to the server
     */
    @Test
    public void testAddJoinRequest() {

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());

        try {
            when(joinRequestService.addJoinRequest(eq(joinRequest))).thenReturn(joinRequest);
            mockMvc.perform(MockMvcRequestBuilders.post("/joinRequest")
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(joinRequestService, Mockito.times(1)).addJoinRequest(eq(joinRequest));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }

    }

}
