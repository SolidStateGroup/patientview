package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.ExternalConversation;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Conversation service, for operations related to Conversations and Messages.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
public interface ConversationService {

    /**
     * Create a new conversation, including recipients and associated Message.
     * @param userId ID of User creating Conversation
     * @param conversation Conversation object containing all required properties and first Message content
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void addConversation(Long userId, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Creates a conversation between a User with userId and all staff members with a specific Feature with
     * featureName. Used in EQ5D survey page (Your Overall Health) when patients send feedback to staff users.
     * @param userId Long ID of User with survey
     * @param featureName Name of feature that staff Users must have to be added to conversation
     * @param conversation Conversation, containing user entered message to staff users
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void addConversationToRecipientsByFeature(Long userId, String featureName, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException, VerificationException;

    /**
     * Add a User to a Conversation by creating a new ConversationUser with ConversationLabel.INBOX.
     * @param conversationId ID of Conversation to add User to
     * @param userId ID of User to be added to Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void addConversationUser(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a label to a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to add Conversation label to
     * @param conversationId ID of Conversation to add label to
     * @param conversationLabel ConversationLabel label to add to Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void addConversationUserLabel(Long userId, Long conversationId, ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Create a Conversation for a User or set of Users, used by external systems
     * @param externalConversation ExternalConversation, contains all required fields to generate a Conversation
     * @return ExternalConversation, including any required confirmation
     */
    ExternalConversation addExternalConversation(ExternalConversation externalConversation);

    /**
     * Add a Message to an existing Conversation.
     * @param conversationId ID of Conversation to add Message to
     * @param message Message object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void addMessage(Long conversationId, org.patientview.api.model.Message message)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a read receipt for a Message given the Message and User IDs.
     * @param messageId ID of Message to add read receipt for
     * @param userId ID of User who has read the Message
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void addMessageReadReceipt(Long messageId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a User from all Conversations, used during User deletion.
     * @param user User to delete from all Conversations
     */
    void deleteUserFromConversations(User user);

    /**
     * Get a Conversation, including Messages given a Conversation ID.
     * @param conversationId ID of Conversation to retrieve
     * @return Conversation object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    org.patientview.api.model.Conversation findByConversationId(Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Page of Conversation objects given a User (who is a member of the Conversations).
     * @param userId ID of User to retrieve Conversations for
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * @return Page of Conversation objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    Page<org.patientview.api.model.Conversation> findByUserId(Long userId, GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Conversation User's picture, returned as byte[] to allow direct viewing in browser when set as img source.
     * Will only retrieve picture if current user is a member of conversation.
     * @param conversationId ID of User to retrieve picture for
     * @param userId ID of User to retrieve picture for
     * @return byte[] binary picture data
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    byte[] getConversationUserPicture(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of BaseUser (used as Conversation recipients) for a Group based on the feature passed in, currently
     * DEFAULT_MESSAGING_CONTACT. Used when creating a membership request from patients page.
     * @param groupId ID of Group to find available recipients for
     * @param featureName String name of Feature that Users must have to be recipients
     * @return List of BaseUser
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    List<BaseUser> getGroupRecipientsByFeature(Long groupId, String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Given a user Id and Feature name, get the number of staff Users that have the Feature in the User's Groups,
     * used by EQ5D survey page (overall health)
     * @param userId ID of User to get count of recipients for
     * @param featureName String name of Feature
     * @return Long count of recipients
     * @throws ResourceNotFoundException
     */
    @UserOnly
    Long getStaffRecipientCountByFeature(Long userId, String featureName) throws ResourceNotFoundException;

    /**
     * Get the specificied message by id
     *
     * @param messageId ID of the message to get
     * @return Message that has ben found
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    Message getMessageById(Long messageId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Fast method of returning available Conversation recipients when a User has selected a Group in the UI.
     * Note: returns HTML as a String to avoid performance issues in ie8
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return HTML String for drop-down select
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    String getRecipientsAsHtml(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get available Conversation recipients by a Group.
     * Used in mobile api.
     *
     * @param userId  ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return a list of BaseUsers grouped by user group
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    Map<String, List<BaseUser>> getRecipientsList(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get the number of unread Messages (those with no read receipt) for a User.
     * @param userId ID of User to find number of unread messages for
     * @return Long containing number of unread messages
     * @throws ResourceNotFoundException
     */
    @UserOnly
    Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException;

    /**
     * Remove a User from a Conversation by deleting the ConversationUser.
     * @param conversationId ID of Conversation to remove User from
     * @param userId ID of User to be removed from Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void removeConversationUser(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Remove a label from a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to remove Conversation label from
     * @param conversationId ID of Conversation to add label from
     * @param conversationLabel ConversationLabel label to remove from Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void removeConversationUserLabel(Long userId, Long conversationId, ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException;
}
