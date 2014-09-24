package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.IdentifierService;
import org.patientview.persistence.model.Identifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
public class IdentifierControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private IdentifierService identifierService;

    @InjectMocks
    private IdentifierController identifierController;

    private MockMvc mockMvc;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(identifierController).build();
    }

    @Test
    public void testUpdateIdentifier() {
        Identifier testIdentifier = new Identifier();
        testIdentifier.setId(1L);

        try {
            when(identifierService.save(eq(testIdentifier))).thenReturn(testIdentifier);
            mockMvc.perform(MockMvcRequestBuilders.put("/identifier")
                    .content(mapper.writeValueAsString(testIdentifier)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(identifierService, Mockito.times(1)).saveIdentifier(eq(testIdentifier));
        } catch (Exception e) {
            fail("This call should not fail");
        }
    }

    @Test
    public void testDeleteIdentifier() {
        Long identifierId = 1L;
        String url = "/identifier/" + identifierId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }

        verify(identifierService, Mockito.times(1)).delete(eq(identifierId));
    }



}
