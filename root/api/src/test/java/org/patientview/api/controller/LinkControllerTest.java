package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.LinkService;
import org.patientview.persistence.model.Link;
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
 * Created on 15/07/2014
 */
public class LinkControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private LinkService linkService;

    @InjectMocks
    private LinkController linkController;

    private MockMvc mockMvc;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(linkController).build();
    }

    @Test
    public void testCreateLink() {
        Link testLink = new Link();
        testLink.setId(1L);

        try {
            when(linkService.add(eq(testLink))).thenReturn(testLink);
            mockMvc.perform(MockMvcRequestBuilders.post("/link")
                    .content(mapper.writeValueAsString(testLink)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(linkService, Mockito.times(1)).add(eq(testLink));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testUpdateLink() {
        Link testLink = new Link();
        testLink.setId(1L);

        try {
            when(linkService.save(eq(testLink))).thenReturn(testLink);
            mockMvc.perform(MockMvcRequestBuilders.put("/link")
                    .content(mapper.writeValueAsString(testLink)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(linkService, Mockito.times(1)).save(eq(testLink));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testDeleteLink() {
        Long linkId = 1L;
        String url = "/link/" + linkId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isNoContent());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(linkService, Mockito.times(1)).delete(eq(linkId));
    }



}
