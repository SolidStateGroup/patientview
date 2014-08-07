package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConversationService extends CrudService<Conversation> {

    Page<Conversation> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException;

    void addMessage(Long conversationId, Message message) throws ResourceNotFoundException;

    void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException;

}
