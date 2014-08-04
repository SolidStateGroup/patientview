package org.patientview.api.controller;

import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class JoinRequestController extends BaseController<JoinRequestController> {

    @Inject
    private JoinRequestService joinRequestService;


    public List<JoinRequestStatus> getJoinRequestStatuses() {
        return null;
    }

}
