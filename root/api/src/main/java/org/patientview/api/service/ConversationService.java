package org.patientview.api.service;

import org.patientview.api.model.User;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConversationService extends CrudService<Conversation> {

    org.patientview.api.model.Conversation findByConversationId(Long conversationId) throws ResourceNotFoundException;

    Page<org.patientview.api.model.Conversation> findByUserId(Long userId, Pageable pageable)
            throws ResourceNotFoundException;

    void addMessage(Long conversationId, org.patientview.api.model.Message message) throws ResourceNotFoundException;

    void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException;

    void addMessageReadReceipt(Long messageId, Long userId) throws ResourceNotFoundException;

    Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException;

    List<User> getRecipients(Long userId, String[] featureTypes)
            throws ResourceNotFoundException, ResourceInvalidException;
}
