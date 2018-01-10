package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.ExternalConversation;
import org.patientview.api.service.ConversationService;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */

public class ConversationControllerTest {

    @InjectMocks
    private ConversationController conversationController;

    @Mock
    private ConversationService conversationService;

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UriComponentsBuilder uriComponentsBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(conversationController, uriComponentsBuilder).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetUserConversations() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/conversations?size=5&page=0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testAddConversationToRecipientsByFeature() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Feature feature = TestUtils.createFeature(FeatureType.RENAL_SURVEY_FEEDBACK_RECIPIENT.toString());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/conversations/feature/"
                + feature.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new Conversation())))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testAddConversationUser() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        
        User user2 = TestUtils.createUser("test2User");

        Conversation conversation = new Conversation();
        conversation.setId(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/conversation/"
                + conversation.getId() + "/conversationuser/" + user2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversationService, Mockito.times(1))
                .addConversationUser(eq(conversation.getId()), eq(user2.getId()));
    }

    @Test
    public void testAddConversationUserLabel() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        
        Conversation conversation = new Conversation();
        conversation.setId(1L);

        ConversationLabel conversationLabel = ConversationLabel.ARCHIVED;

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/conversations/"
                + conversation.getId() + "/conversationlabel/" + conversationLabel.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testAddExternalConversation() throws Exception {
        ExternalConversation externalConversation = new ExternalConversation();

        mockMvc.perform(MockMvcRequestBuilders.post("/conversations/external")
                .content(mapper.writeValueAsString(externalConversation))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversationService, Mockito.times(1)).addExternalConversation(any(ExternalConversation.class));
    }

    @Test
    public void testRemoveConversationUser() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        User user2 = TestUtils.createUser("test2User");

        Conversation conversation = new Conversation();
        conversation.setId(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/conversation/"
                + conversation.getId() + "/conversationuser/" + user2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversationService, Mockito.times(1))
                .removeConversationUser(eq(conversation.getId()), eq(user2.getId()));
    }

    @Test
    public void testRemoveConversationUserLabel() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Conversation conversation = new Conversation();
        conversation.setId(1L);

        ConversationLabel conversationLabel = ConversationLabel.ARCHIVED;

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + user.getId() + "/conversations/"
                + conversation.getId() + "/conversationlabel/" + conversationLabel.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversationService, Mockito.times(1))
                .removeConversationUserLabel(eq(user.getId()), eq(conversation.getId()), eq(conversationLabel));
    }

    @Test
    public void testGetGroupRecipientsByFeature() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        
        Feature feature = TestUtils.createFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/group/" + group.getId() + "/recipientsbyfeature/"
                + feature.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversationService, Mockito.times(1))
                .getGroupRecipientsByFeature(eq(group.getId()), eq(feature.getName()));
    }

    @Test
    public void testGetStaffRecipientCountByFeature() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Feature feature = TestUtils.createFeature(FeatureType.RENAL_SURVEY_FEEDBACK_RECIPIENT.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId()
                + "/conversations/staffrecipientcountbyfeature/"
                + feature.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetRecipients() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/conversations/"
                +  "/recipients/list?groupId="+group.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
