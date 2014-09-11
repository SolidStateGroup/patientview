package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationHeadingControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ObservationHeadingService observationHeadingService;

    @Mock
    private ObservationHeadingRepository observationHeadingRepository;

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

    @Test
    public void testGet() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/observationheading/" + observationHeading.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception thrown");
        }

        try {
            verify(observationHeadingService, Mockito.times(1)).get(eq(observationHeading.getId()));
        } catch (ResourceNotFoundException rnf) {
            fail("ResourceNotFoundException thrown");
        }
    }

    @Test
    public void testAdd() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        try {
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);

            mockMvc.perform(MockMvcRequestBuilders.post("/observationheading")
                    .content(mapper.writeValueAsString(observationHeading))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).add(eq(observationHeading));
        } catch (Exception e) {
            fail("Exception thrown");
        }
    }

    @Test
    public void testSave() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        try {
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);

            mockMvc.perform(MockMvcRequestBuilders.put("/observationheading")
                    .content(mapper.writeValueAsString(observationHeading))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).save(eq(observationHeading));
        } catch (Exception e) {
            fail("Exception thrown");
        }
    }
}


