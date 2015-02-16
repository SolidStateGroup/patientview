package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.Conversation;
import org.patientview.api.model.Message;
import org.patientview.api.service.ConversationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.HashMap;
import java.util.List;

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
     * Get a Conversation, including Messages given a Conversation ID.
     * @param conversationId ID of Conversation to retrieve
     * @return
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
     * Get a Page of Conversation objects given a User (who is a member of the Conversations). Note: simplified
     * pagination using just page size and page number as parameters.
     * @param userId ID of User to retrieve Conversations for
     * @param size Integer size of page (for pagination)
     * @param page Integer page number (for pagination)
     * @return Page of Conversation objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @CacheEvict(value = "unreadConversationCount", allEntries = true)
    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Conversation>> getConversations(
            @PathVariable("userId") Long userId, @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page)
            throws ResourceNotFoundException, ResourceForbiddenException {
        Integer pageConverted = null, sizeConverted = null;
        PageRequest pageable;

        if (StringUtils.isNotEmpty(page)) {
            pageConverted = Integer.parseInt(page);
        }

        if (StringUtils.isNotEmpty(size)) {
            sizeConverted = Integer.parseInt(size);
        }

        if (pageConverted != null && sizeConverted != null) {
            pageable = new PageRequest(pageConverted, sizeConverted);
        } else {
            pageable = new PageRequest(0, Integer.MAX_VALUE);
        }

        return new ResponseEntity<>(conversationService.findByUserId(userId, pageable), HttpStatus.OK);
    }

    /**
     * Get a list of potential message recipients, mapped by User role. Used in UI by user when creating a new 
     * Conversation to populate the dropdown of available recipients after a Group is selected. 
     * Note: not currently used due to speed concerns when rendering large lists client-side in ie8. 
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return Object containing Lists of BaseUser organised by Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/recipients", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<HashMap<String, List<BaseUser>>> getRecipients(@PathVariable("userId") Long userId,
            @RequestParam(value = "groupId", required = false) Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.getRecipients(userId, groupId), HttpStatus.OK);
    }

    /**
     * Fast method of returning available Conversation recipients when a User has selected a Group in the UI.
     * Note: returns HTML as a String to avoid performance issues in ie8
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return HTML String for dropdown select
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations/recipientsfast", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getRecipientsFast(@PathVariable("userId") Long userId,
            @RequestParam(value = "groupId", required = false) Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.getRecipientsFast(userId, groupId), HttpStatus.OK);
    }

    /**
     * Get the number of unread Messages (those with no read receipt) for a User.
     * @param userId ID of User to find number of unread messages for
     * @return Long containing number of unread messages
     * @throws ResourceNotFoundException
     */
    @Cacheable(value = "unreadConversationCount")
    @RequestMapping(value = "/user/{userId}/conversations/unreadcount", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> getUnreadConversationCount(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(conversationService.getUnreadConversationCount(userId), HttpStatus.OK);
    }

    /**
     * Create a new conversation, including recipients and associated Message.
     * @param userId ID of User creating Conversation
     * @param conversation Conversation object containing all required properties and first Message content
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newConversation(@PathVariable("userId") Long userId,
            @RequestBody org.patientview.persistence.model.Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addConversation(userId, conversation);
    }

    /**
     * Add a read receipt for a Message given the Message and User IDs.
     * @param messageId ID of Message to add read receipt for
     * @param userId ID of User who has read the Message
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/message/{messageId}/readreceipt/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addMessageReadReceipt(@PathVariable("messageId") Long messageId, @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addMessageReadReceipt(messageId, userId);
    }
}
