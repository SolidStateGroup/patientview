package org.patientview.api.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationHeadingControllerTest {

    @Mock
    private ObservationHeadingService observationHeadingService;

    @Mock
    private ObservationHeadingRepository observationHeadingRepository;

    @InjectMocks
    private SecurityAspect securityAspect = SecurityAspect.aspectOf();

    @InjectMocks
    private ObservationHeadingController observationHeadingController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationHeadingController).build();
    }

    @Test
    public void testFindAll() {
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/observationheading?page=0&size=5&sortDirection=ASC&sortField=name")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }
}


