package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.ConversationServiceImpl;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.MessageTypes;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    MessageRepository messageRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ConversationService conversationService = new ConversationServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateConversation() {

        User user1 = TestUtils.createUser("newTestUser1");
        User user2 = TestUtils.createUser("newTestUser2");

        TestUtils.authenticateTest(user1, Collections.EMPTY_LIST);

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

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);

        TestUtils.authenticateTest(user1, Collections.EMPTY_LIST);

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }

    @Test
    public void testGetUnreadConversationCount() {

        User user1 = TestUtils.createUser("newTestUser1");
        User user2 = TestUtils.createUser("newTestUser2");

        TestUtils.authenticateTest(user1, Collections.EMPTY_LIST);

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
        message.setReadReceipts(new HashSet<MessageReadReceipt>());

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        List<Conversation> conversationList = new ArrayList<>();
        conversationList.add(conversation);
        PageRequest pageRequestAll = new PageRequest(0, Integer.MAX_VALUE);
        Page<Conversation> conversationPage = new PageImpl<>(conversationList, pageRequestAll, conversationList.size());

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);

        when(userRepository.exists(Matchers.eq(user1.getId()))).thenReturn(true);
        when(userRepository.exists(Matchers.eq(user2.getId()))).thenReturn(true);

        when(conversationRepository.getUnreadConversationCount(eq(user1.getId()))).thenReturn(1L);

        TestUtils.authenticateTest(user1, Collections.EMPTY_LIST);

        try {
            Assert.assertEquals("Should get 1 unread conversation", (Long)1L,
                    conversationService.getUnreadConversationCount(user1.getId()));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }
}
