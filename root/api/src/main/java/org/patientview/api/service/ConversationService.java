package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseUser;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConversationService extends CrudService<Conversation> {

    org.patientview.api.model.Conversation findByConversationId(Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    Page<org.patientview.api.model.Conversation> findByUserId(Long userId, Pageable pageable)
            throws ResourceNotFoundException;

    void addMessage(Long conversationId, org.patientview.api.model.Message message)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException;

    void addMessageReadReceipt(Long messageId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException;

    @UserOnly
    HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;
}
