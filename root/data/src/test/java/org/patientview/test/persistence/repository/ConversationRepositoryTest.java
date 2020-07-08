package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.ConversationUserLabel;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class ConversationRepositoryTest {

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private DataTestUtils dataTestUtils;

    @Test
    public void testCreateConversation() {

        User user1 = dataTestUtils.createUser("user1");
        User user2 = dataTestUtils.createUser("user2");

        Conversation conversation = new Conversation();

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setUser(user1);
        conversationUser1.setAnonymous(false);
        conversationUser1.setConversation(conversation);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setUser(user2);
        conversationUser2.setAnonymous(false);
        conversationUser2.setConversation(conversation);

        conversation.setConversationUsers(new HashSet<ConversationUser>());
        conversation.getConversationUsers().add(conversationUser1);
        conversation.getConversationUsers().add(conversationUser2);

        conversation.setTitle("test conversation");

        Conversation entityConversation = conversationRepository.save(conversation);

        Assert.assertTrue("Should have 2 ConversationUser attached", entityConversation.getConversationUsers().size() == 2);
    }


    @Test
    public void testFindConversationByUser() {

        User user1 = dataTestUtils.createUser("user1");
        User user2 = dataTestUtils.createUser("user2");

        Conversation conversation = new Conversation();

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setUser(user1);
        conversationUser1.setAnonymous(false);
        conversationUser1.setConversation(conversation);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setUser(user2);
        conversationUser2.setAnonymous(false);
        conversationUser2.setConversation(conversation);

        conversation.setConversationUsers(new HashSet<ConversationUser>());
        conversation.getConversationUsers().add(conversationUser1);
        conversation.getConversationUsers().add(conversationUser2);

        PageRequest pageable = PageRequest.of(0, 5);

        conversation.setTitle("test conversation");

        Conversation entityConversation = conversationRepository.save(conversation);
        Assert.assertTrue("Should have 2 ConversationUser attached", entityConversation.getConversationUsers().size() == 2);

        Page<Conversation> entityConversations = conversationRepository.findByUser(user1, pageable);
        Assert.assertTrue("Should find 1 Conversation for user", entityConversations.getContent().size() == 1);
    }

    @Test
    public void testFindConversationByUserPaging() {

        User user1 = dataTestUtils.createUser("user1");
        User user2 = dataTestUtils.createUser("user2");

        for (int i=0; i<30; i++) {
            Conversation conversation = new Conversation();

            ConversationUser conversationUser1 = new ConversationUser();
            conversationUser1.setUser(user1);
            conversationUser1.setAnonymous(false);
            conversationUser1.setConversation(conversation);

            ConversationUser conversationUser2 = new ConversationUser();
            conversationUser2.setUser(user2);
            conversationUser2.setAnonymous(false);
            conversationUser2.setConversation(conversation);

            conversation.setConversationUsers(new HashSet<ConversationUser>());
            conversation.getConversationUsers().add(conversationUser1);
            conversation.getConversationUsers().add(conversationUser2);

            conversation.setTitle(String.valueOf(i+1));
            conversation.setLastUpdate(new Date());

            conversationRepository.save(conversation);
        }

        PageRequest pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Conversation> entityConversations = conversationRepository.findByUser(user1, pageableAll);
        Assert.assertTrue("Should find 30 Conversations for user", entityConversations.getContent().size() == 30);

        PageRequest pageablePage2 = PageRequest.of(2, 5);
        entityConversations = conversationRepository.findByUser(user1, pageablePage2);
        Assert.assertTrue("Should find 5 Conversations for user", entityConversations.getContent().size() == 5);
    }

    @Test
    public void testFindUnreadConversationCount() {

        User user1 = dataTestUtils.createUser("user1");
        User user2 = dataTestUtils.createUser("user2");

        for (int i=0; i<30; i++) {
            Conversation conversation = new Conversation();

            ConversationUser conversationUser1 = new ConversationUser();
            conversationUser1.setUser(user1);
            conversationUser1.setAnonymous(false);
            conversationUser1.setConversation(conversation);

            ConversationUser conversationUser2 = new ConversationUser();
            conversationUser2.setUser(user2);
            conversationUser2.setAnonymous(false);
            conversationUser2.setConversation(conversation);

            conversation.setConversationUsers(new HashSet<ConversationUser>());
            conversation.getConversationUsers().add(conversationUser2);

            conversation.setTitle(String.valueOf(i+1));
            conversation.setLastUpdate(new Date());

            conversation.setMessages(new ArrayList<Message>());

            // user1 has messages on last 25
            // has read 20 of them
            // 5 are archived
            if (i > 4) {
                conversation.getConversationUsers().add(conversationUser1);
                Message message = new Message();
                message.setUser(user1);
                message.setReadReceipts(new HashSet<MessageReadReceipt>());
                message.setConversation(conversation);

                if (i > 9) {
                    MessageReadReceipt readReceipt = new MessageReadReceipt();
                    readReceipt.setUser(user1);
                    readReceipt.setMessage(message);
                    readReceipt.setCreated(new Date());
                    message.getReadReceipts().add(readReceipt);
                }

                conversation.getMessages().add(message);

                // set first 20 with this user to inbox, 5 to archived
                if (i < 25) {
                    Set<ConversationUserLabel> conversationUserLabelSet = new HashSet<>();
                    conversationUserLabelSet.add(
                            new ConversationUserLabel(conversationUser1, ConversationLabel.INBOX));
                    conversation.getConversationUsers().iterator().next()
                            .setConversationUserLabels(conversationUserLabelSet);
                } else {
                    Set<ConversationUserLabel> conversationUserLabelSet = new HashSet<>();
                    conversationUserLabelSet.add(
                            new ConversationUserLabel(conversationUser1, ConversationLabel.ARCHIVED));
                    conversation.getConversationUsers().iterator().next()
                            .setConversationUserLabels(conversationUserLabelSet);
                }
            }


            conversationRepository.save(conversation);
        }

        PageRequest pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Conversation> entityConversations = conversationRepository.findByUser(user1, pageableAll);
        Assert.assertEquals("Should find 25 Conversations for user", 25, entityConversations.getContent().size());

        Long count = conversationRepository.getUnreadConversationCount(user1.getId());
        Assert.assertEquals("Should find 5 unread Conversations for user", (Long) 5L, count);
    }
}
