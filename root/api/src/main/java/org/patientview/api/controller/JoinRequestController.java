package org.patientview.api.controller;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class JoinRequestController {

    private final static Logger LOG = LoggerFactory.getLogger(JoinRequestController.class);

    @Inject
    private JoinRequestService joinRequestService;

    @RequestMapping(value = "/joinRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addJoinRequest(@RequestBody JoinRequest joinRequest) throws ResourceNotFoundException{
        LOG.debug("Join Request Received for {} {}", joinRequest.getForename(), joinRequest.getSurname());

        joinRequestService.addJoinRequest(joinRequest);

        return new ResponseEntity<Void>(HttpStatus.CREATED);

    }
}
