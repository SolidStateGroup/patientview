package org.patientview.api.controller;

import org.patientview.api.service.JoinRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


}
