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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
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
            throw new ResourceNotFoundException("Could not find conversation {}" + conversation.getId());
        }

        // TODO: save conversation fields
        return conversationRepository.save(entityConversation);
    }

    public void delete(Long conversationId) {
        conversationRepository.delete(conversationId);
    }

    public Page<Conversation> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }

        return conversationRepository.findByUser(entityUser, pageable);
    }

    public void addMessage(Long conversationId, Message message) throws ResourceNotFoundException {
        Conversation entityConversation = conversationRepository.findOne(conversationId);
        if (entityConversation == null) {
            throw new ResourceNotFoundException("Could not find conversation {}" + conversationId);
        }

        User entityUser = userRepository.findOne(message.getUser().getId());
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Message newMessage = new Message();
        newMessage.setUser(entityUser);
        newMessage.setConversation(entityConversation);
        newMessage.setMessage(message.getMessage());
        newMessage.setType(message.getType());

        entityConversation.getMessages().add(newMessage);
        conversationRepository.save(entityConversation);
    }

    public void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException {

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

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

        Set<Message> messageSet = new HashSet<>();
        messageSet.add(newMessage);
        newConversation.setMessages(messageSet);

        // set conversation users
        Set<ConversationUser> conversationUserSet = new HashSet<>();

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            ConversationUser newConversationUser = new ConversationUser();
            newConversationUser.setConversation(newConversation);

            entityUser = userRepository.findOne(conversationUser.getUser().getId());
            if (entityUser == null) {
                throw new ResourceNotFoundException("Could not find user");
            }

            newConversationUser.setUser(entityUser);
            newConversationUser.setAnonymous(conversationUser.getAnonymous());
            newConversationUser.setCreator(userRepository.findOne(creator.getId()));
            conversationUserSet.add(newConversationUser);
        }

        newConversation.setConversationUsers(conversationUserSet);

        // persist conversation
        conversationRepository.save(newConversation);
    }

    public void addMessageReadReceipt(Long messageId, Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Message entityMessage = messageRepository.findOne(messageId);
        if (entityMessage == null) {
            throw new ResourceNotFoundException("Could not find message");
        }

        boolean found = false;
        for (MessageReadReceipt messageReadReceipt : entityMessage.getReadReceipts()) {
            if (messageReadReceipt.getUser().equals(entityUser)) {
                found = true;
            }
        }

        if (!found) {
            MessageReadReceipt messageReadReceipt = new MessageReadReceipt();
            messageReadReceipt.setUser(entityUser);
            messageReadReceipt.setMessage(entityMessage);
            entityMessage.getReadReceipts().add(messageReadReceipt);
            messageRepository.save(entityMessage);
        }
    }
}
