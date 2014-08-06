package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.ConversationService;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import java.util.List;

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

    public List<Conversation> findByUserId(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }

        return conversationRepository.findByUser(entityUser);
    }
}
