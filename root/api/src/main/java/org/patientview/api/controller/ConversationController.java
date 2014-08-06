package org.patientview.api.controller;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.ConversationService;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014.
 */
@RestController
public class ConversationController extends BaseController<ConversationController> {

    private final static Logger LOG = LoggerFactory.getLogger(ConversationController.class);

    @Inject
    private ConversationService conversationService;

    @RequestMapping(value = "/conversation/{conversationId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Conversation> getConversation(@PathVariable("conversationId") Long conversationId) throws SecurityException {
        try {
            LOG.debug("Request has been received for conversation with id : {}", conversationId);
            Conversation conversation = conversationService.get(conversationId);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/user/{userId}/conversations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Conversation>> getConversations(@PathVariable("userId") Long userId) {
        try {
            LOG.debug("Request has been received for conversations of userId : {}", userId);
            List<Conversation> conversations = conversationService.findByUserId(userId);
            return new ResponseEntity<>(conversations, HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/conversation/{conversationId}/messages", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addMessage(@PathVariable("conversationId") Long conversationId,
                                            @RequestBody Message message, UriComponentsBuilder uriComponentsBuilder) {
        try {
            conversationService.addMessage(conversationId, message);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
