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
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.ConversationService;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.User;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */

public class ConversationControllerTest {

    @Mock
    private UriComponentsBuilder uriComponentsBuilder;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ConversationController conversationController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(conversationController, uriComponentsBuilder).build();
    }

    @Test
    public void testGetUserConversations() {
        User testUser = TestUtils.createUser(1L, "testPost");
        Conversation conversation = new Conversation();
        conversation.setId(2L);
        List<Conversation> conversationList = new ArrayList<>();
        conversationList.add(conversation);

        try {
            when(conversationService.findByUserId(Matchers.eq(testUser.getId()))).thenReturn(conversationList);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("Getting conversations should not fail.");
        }

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + testUser.getId() + "/conversations")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        try {
            verify(conversationService, Mockito.times(1)).findByUserId(eq(testUser.getId()));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("Verifying conversation set should not fail.");
        }
    }
}
