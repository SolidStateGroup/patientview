package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class ConversationRepositoryTest {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

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

        conversation.setTitle("test conversation");

        Conversation entityConversation = conversationRepository.save(conversation);
        Assert.assertTrue("Should have 2 ConversationUser attached", entityConversation.getConversationUsers().size() == 2);

        List<Conversation> entityConversations = conversationRepository.findByUser(user1);
        Assert.assertTrue("Should find 1 Conversation for user", entityConversations.size() == 1);
    }
}
