package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.JoinRequestService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class JoinRequestController extends BaseController<JoinRequestController> {

    private static final Logger LOG = LoggerFactory.getLogger(JoinRequestController.class);

    @Inject
    private JoinRequestService joinRequestService;

    @RequestMapping(value = "/public/joinrequest", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addJoinRequest(@RequestBody JoinRequest joinRequest)
            throws ResourceNotFoundException, ResourceForbiddenException {
        joinRequestService.add(joinRequest);
    }

    /*
    @RequestMapping(value = "/migrate/joinrequests", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void migrateJoinRequests(@RequestBody List<JoinRequest> joinRequests) throws ResourceNotFoundException {
        joinRequestService.migrate(joinRequests);
    }
    */

    @RequestMapping(value = "/joinrequest/statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<JoinRequestStatus> getStatuses() {
        return CollectionUtils.arrayToList(JoinRequestStatus.values());
    }

    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.JoinRequest>> getByUser(@PathVariable("userId") Long userId
            , GetParameters getParameters) throws ResourceNotFoundException {
        return new ResponseEntity<>(joinRequestService.getByUser(userId, getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/joinrequest", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> save(@RequestBody JoinRequest joinRequest) throws ResourceNotFoundException {
        joinRequestService.save(joinRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/joinrequest/{joinRequestId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.JoinRequest> get(@PathVariable("joinRequestId") Long joinRequestId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(joinRequestService.get(joinRequestId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/joinrequests/count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BigInteger> getSubmittedJoinRequestCount(@PathVariable("userId") Long userId) {
        try {
            LOG.debug("Request has been received for conversations of userId : {}", userId);
            return new ResponseEntity<>(joinRequestService.getCount(userId), HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
