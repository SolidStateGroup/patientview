package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseUser;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Conversation service, for CRUD operations related to Conversations and Messages.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConversationService extends CrudService<Conversation> {

    @UserOnly
    void addConversation(Long userId, Conversation conversation)
    throws ResourceNotFoundException, ResourceForbiddenException;

    void addMessage(Long conversationId, org.patientview.api.model.Message message)
    throws ResourceNotFoundException, ResourceForbiddenException;

    void addMessageReadReceipt(Long messageId, Long userId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    void deleteUserFromConversations(User user);

    @UserOnly
    Page<org.patientview.api.model.Conversation> findByUserId(Long userId, Pageable pageable)
            throws ResourceNotFoundException, ResourceForbiddenException;

    org.patientview.api.model.Conversation findByConversationId(Long conversationId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    String getRecipientsFast(Long userId, Long groupId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException;
}
