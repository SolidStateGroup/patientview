package org.patientview.api.controller;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
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
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class JoinRequestController extends BaseController<JoinRequestController> {

    @Inject
    private JoinRequestService joinRequestService;

    @RequestMapping(value = "/joinrequest/statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<JoinRequestStatus> getStatuses() {
        return CollectionUtils.arrayToList(JoinRequestStatus.values());
    }

    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<JoinRequest>> get(@PathVariable("userId") Long userId,
                                                            @RequestParam(value = "status", required = false)
                                                            String status)
            throws ResourceNotFoundException {
        if (status == null) {
            return new ResponseEntity(joinRequestService.get(userId), HttpStatus.OK);
        }

        return new ResponseEntity(joinRequestService.getByStatus(userId,
                JoinRequestStatus.valueOf(status)), HttpStatus.OK);

    }

    @RequestMapping(value = "/joinrequest", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> save(@RequestBody JoinRequest joinRequest)
        throws ResourceNotFoundException{
        joinRequestService.save(joinRequest);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
