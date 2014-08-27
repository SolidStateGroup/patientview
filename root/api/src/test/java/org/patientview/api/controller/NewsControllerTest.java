package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.NewsItem;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test associated with news
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public class NewsControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsController newsController;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(newsController).build();

    }

    /**
     * Test: Send a GET request with a long parameter to the new service to return news
     * Fail: The service does not get called with the parameter
     *
     * TODO test needs expanding into testing returned data
     */
    @Test
    public void testNewsByUser() {
        Long testUserId = 1L;
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);

        try {
            when(newsService.findByUserId(eq(testUserId), eq(pageable))).thenReturn(new PageImpl<>(new ArrayList<NewsItem>()));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException throw");
        }

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + Long.toString(testUserId) + "/news"))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        try {
            verify(newsService, Mockito.times(1)).findByUserId(Matchers.eq(testUserId), Matchers.eq(pageable));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException throw");
        }
    }

    @Test
    public void testUpdateNews() {
        NewsItem testNews = new NewsItem();
        testNews.setId(1L);

        try {
            when(newsService.save(eq(testNews))).thenReturn(testNews);
            mockMvc.perform(MockMvcRequestBuilders.put("/news")
                    .content(mapper.writeValueAsString(testNews)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(newsService, Mockito.times(1)).save(eq(testNews));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }
}
