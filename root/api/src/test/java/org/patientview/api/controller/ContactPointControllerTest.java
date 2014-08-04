package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceInvalidException;
import org.patientview.api.service.ContactPointService;
import org.patientview.persistence.model.ContactPoint;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 31/07/2014
 */
public class ContactPointControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ContactPointService contactPointService;

    @InjectMocks
    private ContactPointController contactPointController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(contactPointController).build();
    }

    /**
     * Test: Simple request to the contact type for a contact point
     * Fail: Doesn't call the service and return OK.
     */
    @Test
    public void testGetGroupByType() throws ResourceInvalidException {
        String type = "testType";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/contactpoint/type/" + type)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(contactPointService, Mockito.times(1)).getContactPointType(eq(type));
    }

    @Test
    public void testUpdateContactPoint() {
        ContactPoint testContactPoint = new ContactPoint();
        testContactPoint.setId(1L);

        try {
            when(contactPointService.saveContactPoint(eq(testContactPoint))).thenReturn(testContactPoint);
            mockMvc.perform(MockMvcRequestBuilders.put("/contactpoint")
                    .content(mapper.writeValueAsString(testContactPoint)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(contactPointService, Mockito.times(1)).saveContactPoint(eq(testContactPoint));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testDeleteContactPoint() {
        Long contactPointId = 1L;
        String url = "/contactpoint/" + contactPointId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isNoContent());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(contactPointService, Mockito.times(1)).deleteContactPoint(eq(contactPointId));
    }
}
