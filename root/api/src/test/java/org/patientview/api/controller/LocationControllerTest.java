package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.LocationService;
import org.patientview.persistence.model.Location;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
public class LocationControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @Test
    public void testCreateLocation() {
        Location testLocation = new Location();
        testLocation.setId(1L);

        try {
            when(locationService.add(eq(testLocation))).thenReturn(testLocation);
            mockMvc.perform(MockMvcRequestBuilders.post("/location")
                    .content(mapper.writeValueAsString(testLocation)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(locationService, Mockito.times(1)).add(eq(testLocation));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testUpdateLocation() {
        Location testLocation = new Location();
        testLocation.setId(1L);

        try {
            when(locationService.save(eq(testLocation))).thenReturn(testLocation);
            mockMvc.perform(MockMvcRequestBuilders.put("/location")
                    .content(mapper.writeValueAsString(testLocation)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(locationService, Mockito.times(1)).save(eq(testLocation));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testDeleteLocation() {
        Long locationId = 1L;
        String url = "/location/" + locationId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isNoContent());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(locationService, Mockito.times(1)).delete(eq(locationId));
    }
}
