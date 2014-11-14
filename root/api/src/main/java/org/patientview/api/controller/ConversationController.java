package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.Conversation;
import org.patientview.api.model.Message;
import org.patientview.api.service.ConversationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014.
 */
@RestController
public class ConversationController extends BaseController<ConversationController> {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationController.class);

    @Inject
    private ConversationService conversationService;

    @RequestMapping(value = "/conversation/{conversationId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.Conversation> getConversation(
            @PathVariable("conversationId") Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.findByConversationId(conversationId), HttpStatus.OK);
    }

    @CacheEvict(value = "unreadConversationCount", allEntries = true)
    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Conversation>> getConversations(
            @PathVariable("userId") Long userId, @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException {

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

        LOG.debug("Request has been received for conversations of userId : {}", userId);
        return new ResponseEntity<>(conversationService.findByUserId(userId, pageable), HttpStatus.OK);
    }

    @Cacheable(value = "unreadConversationCount")
    @RequestMapping(value = "/user/{userId}/conversations/unreadcount", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> getUnreadConversationCount(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(conversationService.getUnreadConversationCount(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/conversations/recipients", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseUser>> getRecipients(@PathVariable("userId") Long userId,
            @RequestParam(value = "featuretype", required = false) String[] featureTypes)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(conversationService.getRecipients(userId, featureTypes), HttpStatus.OK);
    }

    @RequestMapping(value = "/conversation/{conversationId}/messages", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMessage(@PathVariable("conversationId") Long conversationId, @RequestBody Message message)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addMessage(conversationId, message);
    }

    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newConversation(@PathVariable("userId") Long userId,
            @RequestBody org.patientview.persistence.model.Conversation conversation)
            throws ResourceNotFoundException {
        conversationService.addConversation(userId, conversation);
    }

    @RequestMapping(value = "/message/{messageId}/readreceipt/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addMessageReadReceipt(@PathVariable("messageId") Long messageId, @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        conversationService.addMessageReadReceipt(messageId, userId);
    }
}
