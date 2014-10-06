package org.patientview.api.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.DiagnosticService;
import org.patientview.api.service.MedicationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.User;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
public class DiagnosticControllerTest {

    @Mock
    private DiagnosticService diagnosticService;

    @InjectMocks
    private DiagnosticController diagnosticController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(diagnosticController).build();
    }

    @Test
    public void testGetDiagnosticsByUserId() {
        User user = TestUtils.createUser("testuser");
        user.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/diagnostics"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        try {
            verify(diagnosticService, Mockito.times(1)).getByUserId(eq(user.getId()));
        } catch (ResourceNotFoundException | FhirResourceException e) {
            fail("Exception thrown");
        }
    }
}


