package org.patientview.api.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.PatientService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
public class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();

    }

    /**
     * Test: Send a GET request with a long parameter to the patient controller
     * Fail: The service does not get called with the parameter
     *
     */
    @Test
    public void testGetUser() throws ResourceNotFoundException, FhirResourceException {

        String testUserId = "40279dad-5d60-49ec-8f08-f9767e5772af";
        when(patientService.get(eq(UUID.fromString(testUserId)))).thenReturn(null);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/patient/10"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }
}
