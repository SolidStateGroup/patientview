package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.Conversation;
import org.patientview.api.model.ExternalConversation;
import org.patientview.api.model.Message;
import org.patientview.api.service.ConversationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * RESTful interface for Conversations and Messages, including unread count. Conversations have Message objects as
 * children and can be associated with multiple Users.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014.
 */
@RestController
@ExcludeFromApiDoc
public class ConversationController extends BaseController<ConversationController> {

    @Inject
    private ConversationService conversationService;

    /**
     * Create a new conversation, including recipients and associated Message.
     * @param userId ID of User creating Conversation
     * @param conversation Conversation object containing all required properties and first Message content
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addConversation(@PathVariable("userId") Long userId,
                                @RequestBody org.patientview.persistence.model.Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addConversation(userId, conversation);
    }

    /**
     * Creates a conversation between a User with userId and all staff members with a specific Feature with
     * featureName. Used in EQ5D survey page (Your Overall Health) when patients send feedback to staff users.
     * @param userId Long ID of User with survey
     * @param featureName Name of feature that staff Users must have to be added to conversation
     * @param conversation Conversation, containing user entered message to staff users
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/feature/{featureName}", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addConversationToRecipientsByFeature(@PathVariable("userId") Long userId,
            @PathVariable("featureName") String featureName,
            @RequestBody org.patientview.persistence.model.Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException, VerificationException {
        conversationService.addConversationToRecipientsByFeature(userId, featureName, conversation);
    }

    /**
     * Add a User to a Conversation by creating a new ConversationUser with ConversationLabel.INBOX.
     * @param conversationId ID of Conversation to add User to
     * @param userId ID of User to be added to Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/conversation/{conversationId}/conversationuser/{userId}", method = RequestMethod.POST)
    public void addConversationUser(@PathVariable("conversationId") Long conversationId,
                                    @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addConversationUser(conversationId, userId);
    }

    /**
     * Add a label to a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to add Conversation label to
     * @param conversationId ID of Conversation to add label to
     * @param conversationLabel ConversationLabel label to add to Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/{conversationId}/conversationlabel/{conversationLabel}",
            method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addConversationUserLabel(@PathVariable("userId") Long userId,
                                         @PathVariable("conversationId") Long conversationId,
                                         @PathVariable("conversationLabel") ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addConversationUserLabel(userId, conversationId, conversationLabel);
    }

    /**
     * Create a Conversation for a User or set of Users, used by external systems
     * @param externalConversation ExternalConversation, contains all required fields to generate a Conversation
     * @return ExternalConversation, including any required confirmation
     */
    @RequestMapping(value = "/conversations/external", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExternalConversation> addExternalConversation(
            @RequestBody ExternalConversation externalConversation) {
        return new ResponseEntity<>(conversationService.addExternalConversation(externalConversation), HttpStatus.OK);
    }

    /**
     * Add a Message to an existing Conversation.
     * @param conversationId ID of Conversation to add Message to
     * @param message Message object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/conversation/{conversationId}/messages", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMessage(@PathVariable("conversationId") Long conversationId, @RequestBody Message message)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addMessage(conversationId, message);
    }

    /**
     * Add a read receipt for a Message given the Message and User IDs.
     * @param messageId ID of Message to add read receipt for
     * @param userId ID of User who has read the Message
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/message/{messageId}/readreceipt/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public void addMessageReadReceipt(@PathVariable("messageId") Long messageId, @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addMessageReadReceipt(messageId, userId);
    }

    /**
     * Get a Conversation, including Messages given a Conversation ID.
     * @param conversationId ID of Conversation to retrieve
     * @return Conversation object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/conversation/{conversationId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.Conversation> getConversation(
            @PathVariable("conversationId") Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.findByConversationId(conversationId), HttpStatus.OK);
    }

    /**
     * Get a Conversation User's picture, returned as byte[] to allow direct viewing in browser when set as img source.
     * Will only retrieve picture if current user is a member of conversation.
     * @param conversationId ID of User to retrieve picture for
     * @param userId ID of User to retrieve picture for
     * @return byte[] binary picture data
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/conversation/{conversationId}/user/{userId}/picture", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)
    public HttpEntity<byte[]> getPicture(@PathVariable("conversationId") Long conversationId,
                                         @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        byte[] picture = conversationService.getConversationUserPicture(conversationId, userId);
        if (picture != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); //or what ever type it is
            headers.setContentLength(picture.length);
            return new HttpEntity<>(picture, headers);
        } else {
            return new HttpEntity<>(null, null);
        }
    }

    /**
     * Get a Page of Conversation objects given a User (who is a member of the Conversations).
     * @param userId ID of User to retrieve Conversations for
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * @return Page of Conversation objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Conversation>> getConversations(
            @PathVariable("userId") Long userId, GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.findByUserId(userId, getParameters), HttpStatus.OK);
    }

    /**
     * Get a List of BaseUser used as recipients based on the feature passed in, currently DEFAULT_MESSAGING_CONTACT,
     * used when creating a membership request from patients page.
     * @param groupId ID of Group to find available recipients for
     * @param featureName String name of Feature that Users must have to be recipients
     * @return List of BaseUser
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}/recipientsbyfeature/{featureName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseUser>> getGroupRecipientsByFeature(@PathVariable("groupId") Long groupId,
                                         @PathVariable(value = "featureName") String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(
                conversationService.getGroupRecipientsByFeature(groupId, featureName), HttpStatus.OK);
    }

    /**
     * Given a user Id and Feature name, get the number of staff Users that have the Feature in the User's Groups,
     * used by EQ5D survey page (overall health)
     * @param userId ID of User to get count of recipients for
     * @param featureName String name of Feature
     * @return Long count of recipients
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/conversations/staffrecipientcountbyfeature/{featureName}",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> getStaffRecipientCountByFeature(@PathVariable("userId") Long userId,
            @PathVariable(value = "featureName") String featureName) throws ResourceNotFoundException {
        return new ResponseEntity<>(
                conversationService.getStaffRecipientCountByFeature(userId, featureName), HttpStatus.OK);
    }

    /**
     * Return available Conversation recipients when a User has selected a Group in the UI.
     * Note: returns HTML as a String to avoid performance issues in ie8
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return HTML String for drop-down select
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/recipients", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getRecipientsHtml(@PathVariable("userId") Long userId,
            @RequestParam(value = "groupId", required = false) Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.getRecipientsAsHtml(userId, groupId), HttpStatus.OK);
    }

    /**
     * Get available Conversation recipients by a Group.
     * Used for mobile.
     *
     * @param userId  ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return a list of BaseUsers grouped by user group
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/recipients/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, List<BaseUser>>> getRecipients(@PathVariable("userId") Long userId,
                                                                     @RequestParam(value = "groupId",
                                                                             required = false) Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.getRecipientsList(userId, groupId), HttpStatus.OK);
    }

    /**
     * Get the number of unread Messages (those with no read receipt) for a User.
     * @param userId ID of User to find number of unread messages for
     * @return Long containing number of unread messages
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/conversations/unreadcount", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> getUnreadConversationCount(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(conversationService.getUnreadConversationCount(userId), HttpStatus.OK);
    }

    /**
     * Remove a User from a Conversation by deleting the ConversationUser.
     * @param conversationId ID of Conversation to remove User from
     * @param userId ID of User to be removed from Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/conversation/{conversationId}/conversationuser/{userId}", method = RequestMethod.DELETE)
    public void removeConversationUser(@PathVariable("conversationId") Long conversationId,
                                    @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.removeConversationUser(conversationId, userId);
    }

    /**
     * Remove a label from a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to remove Conversation label from
     * @param conversationId ID of Conversation to add label from
     * @param conversationLabel ConversationLabel label to remove from Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/{conversationId}/conversationlabel/{conversationLabel}",
            method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeConversationUserLabel(@PathVariable("userId") Long userId,
                                         @PathVariable("conversationId") Long conversationId,
                                         @PathVariable("conversationLabel") ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.removeConversationUserLabel(userId, conversationId, conversationLabel);
    }
}
