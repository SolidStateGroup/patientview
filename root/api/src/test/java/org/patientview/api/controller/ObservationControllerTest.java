package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public class ObservationControllerTest {

    @Mock
    private ObservationService observationService;

    @InjectMocks
    private ObservationController observationController;

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationController).build();
    }

    @Test
    public void testGetAllObservationsByUserId() {
        User user = TestUtils.createUser("testuser");
        user.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/observations"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        try {
            verify(observationService, Mockito.times(1)).get(eq(user.getId()), any(String.class), any(String.class),
                    any(String.class), any(Long.class));
        } catch (ResourceNotFoundException | FhirResourceException e) {
            fail("Exception thrown");
        }
    }

    @Test
    public void testGetObservationsByUserIdAndCode() {
        User user = TestUtils.createUser("testuser");
        user.setId(1L);
        String code = "EXAMPLE_CODE";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/observations/" + code))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        try {
            verify(observationService, Mockito.times(1)).get(eq(user.getId()), eq(code), any(String.class),
                    any(String.class), any(Long.class));
        } catch (ResourceNotFoundException | FhirResourceException e) {
            fail("Exception thrown");
        }
    }

    @Test
    public void testGetObservationSummaryByUserId() {
        User user = TestUtils.createUser("testuser");
        user.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/observations/summary"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        try {
            verify(observationService, Mockito.times(1)).getObservationSummary(eq(user.getId()));
        } catch (ResourceNotFoundException | FhirResourceException e) {
            fail("Exception thrown");
        }
    }

    @Test
    public void testPostResultSummary() {
        User user = TestUtils.createUser("testuser");
        user.setId(1L);

        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setId(2L);
        observationHeading.setCode("EXAMPLE_CODE");

        UserResultCluster userResultCluster = new UserResultCluster();
        userResultCluster.setValues(new ArrayList<IdValue>());
        IdValue value = new IdValue();
        value.setId(observationHeading.getId());
        value.setValue("99.9");

        List<UserResultCluster> userResultClusters = new ArrayList<>();
        userResultClusters.add(userResultCluster);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/observations/resultclusters")
                    .content(mapper.writeValueAsString(userResultClusters)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        try {
            verify(observationService, Mockito.times(1)).addUserResultClusters(eq(user.getId()), any(List.class));
        } catch (ResourceNotFoundException | FhirResourceException e) {
            fail("Exception thrown");
        }
    }
}


