package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.ConversationServiceImpl;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.MessageTypes;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/08/2014
 */
public class ConversationServiceTest {

    User creator;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ConversationService conversationService = new ConversationServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser(1L, "creator");
    }

    @Test
    public void testCreateConversation() {

        User user1 = TestUtils.createUser(1L, "newTestUser1");
        User user2 = TestUtils.createUser(2L, "newTestUser2");

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);

        Set<Message> messageSet = new HashSet<>();
        messageSet.add(message);
        conversation.setMessages(messageSet);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }

    }
}
