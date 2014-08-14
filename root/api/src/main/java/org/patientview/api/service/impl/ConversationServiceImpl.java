package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.ConversationService;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Service
public class ConversationServiceImpl extends AbstractServiceImpl<ConversationServiceImpl> implements ConversationService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private MessageRepository messageRepository;

    public Conversation get(Long conversationId) {
        return conversationRepository.findOne(conversationId);
    }

    public Conversation add(Conversation conversation) {
        // TODO: add conversation
        return conversationRepository.findOne(conversation.getId());
    }

    public Conversation save(Conversation conversation) throws ResourceNotFoundException {
        Conversation entityConversation = conversationRepository.findOne(conversation.getId());
        if (entityConversation == null) {
            throw new ResourceNotFoundException(String.format("Could not find conversation %s", conversation.getId()));
        }

        // TODO: save conversation fields
        return conversationRepository.save(entityConversation);
    }

    public void delete(Long conversationId) {
        conversationRepository.delete(conversationId);
    }

    public Page<Conversation> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException {
        User entityUser = findEntityUser(userId);
        return conversationRepository.findByUser(entityUser, pageable);
    }

    public void addMessage(Long conversationId, Message message) throws ResourceNotFoundException {
        Conversation entityConversation = conversationRepository.findOne(conversationId);
        if (entityConversation == null) {
            throw new ResourceNotFoundException(String.format("Could not find conversation %s", conversationId));
        }

        User entityUser = findEntityUser(message.getUser().getId());

        Message newMessage = new Message();
        newMessage.setUser(entityUser);
        newMessage.setConversation(entityConversation);
        newMessage.setMessage(message.getMessage());
        newMessage.setType(message.getType());
        newMessage.setReadReceipts(new HashSet<MessageReadReceipt>());
        newMessage.getReadReceipts().add(new MessageReadReceipt(newMessage, entityUser));

        entityConversation.getMessages().add(newMessage);
        entityConversation.setLastUpdate(new Date());

        conversationRepository.save(entityConversation);
    }

    private User findEntityUser(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return entityUser;
    }

    private Set<ConversationUser> createEntityConversationUserSet
            (Set<ConversationUser> conversationUsers, Conversation conversation, User creator)
            throws ResourceNotFoundException {
        Set<ConversationUser> conversationUserSet = new HashSet<>();

        for (ConversationUser conversationUser : conversationUsers) {
            ConversationUser newConversationUser = new ConversationUser();
            newConversationUser.setConversation(conversation);
            newConversationUser.setUser(userRepository.findOne(conversationUser.getUser().getId()));
            newConversationUser.setAnonymous(conversationUser.getAnonymous());
            newConversationUser.setCreator(creator);
            conversationUserSet.add(newConversationUser);
        }

        return conversationUserSet;
    }

    public void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException {

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        creator = userRepository.findOne(creator.getId());
        User entityUser = findEntityUser(userId);

        // create new conversation
        Conversation newConversation = new Conversation();
        newConversation.setTitle(conversation.getTitle());
        newConversation.setImageData(conversation.getImageData());
        newConversation.setOpen(conversation.getOpen());
        newConversation.setRating(conversation.getRating());
        newConversation.setStatus(conversation.getStatus());
        newConversation.setType(conversation.getType());

        // get first message from passed in conversation
        Iterator iter = conversation.getMessages().iterator();
        Message message = (Message)iter.next();

        // set message properties and add to conversation
        Message newMessage = new Message();
        newMessage.setUser(entityUser);
        newMessage.setConversation(newConversation);
        newMessage.setMessage(message.getMessage());
        newMessage.setType(message.getType());
        newMessage.setReadReceipts(new HashSet<MessageReadReceipt>());
        newMessage.getReadReceipts().add(new MessageReadReceipt(newMessage, entityUser));

        Set<Message> messageSet = new HashSet<>();
        messageSet.add(newMessage);
        newConversation.setMessages(messageSet);

        // set conversation users
        newConversation.setConversationUsers(createEntityConversationUserSet(conversation.getConversationUsers(),
                newConversation, creator));

        // set updated, used in UI to order conversations
        newConversation.setLastUpdate(new Date());

        // persist conversation
        conversationRepository.save(newConversation);
    }

    public void addMessageReadReceipt(Long messageId, Long userId) throws ResourceNotFoundException {
        User entityUser = findEntityUser(userId);

        Message entityMessage = messageRepository.findOne(messageId);
        if (entityMessage == null) {
            throw new ResourceNotFoundException(String.format("Could not find message %s", messageId));
        }

        boolean found = false;
        for (MessageReadReceipt messageReadReceipt : entityMessage.getReadReceipts()) {
            if (messageReadReceipt.getUser().equals(entityUser)) {
                found = true;
            }
        }

        if (!found) {
            entityMessage.getReadReceipts().add(new MessageReadReceipt(entityMessage, entityUser));
            messageRepository.save(entityMessage);
        }
    }

    // todo: convert to native query, performance improvements etc
    public int getUnreadConversationCount(Long userId) throws ResourceNotFoundException {
        User entityUser = findEntityUser(userId);
        Page<Conversation> conversationPage = findByUserId(userId, new PageRequest(0, Integer.MAX_VALUE));

        if (conversationPage.getContent().size() == 0) {
            return 0;
        }

        int unreadConversations = 0;

        for (Conversation conversation : conversationPage.getContent()) {
            int unreadMessages = 0;
            for (Message message : conversation.getMessages()) {
                boolean unread = true;
                for (MessageReadReceipt messageReadReceipt : message.getReadReceipts()) {
                    if (messageReadReceipt.getUser().equals(entityUser)) {
                        unread = false;
                    }
                }
                if (unread) {
                    unreadMessages++;
                }
            }
            if (unreadMessages > 0) {
                unreadConversations++;
            }
        }

        return unreadConversations;
    }
}
