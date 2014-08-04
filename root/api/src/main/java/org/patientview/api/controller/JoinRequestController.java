package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<JoinRequestStatus> getJoinRequestStatuses() {
        return CollectionUtils.arrayToList(JoinRequestStatus.values());
    }

    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<JoinRequest>> getJoinRequest(@PathVariable("userId") Long userId,
                                                            @RequestParam(value = "statuses", required = false)
                                                            Set<String> statuses)
            throws ResourceNotFoundException {
        return new ResponseEntity(joinRequestService.getByType(userId, getStatuses(statuses)), HttpStatus.OK);
    }


    @RequestMapping(value = "/joinrequest", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addChildGroup(@RequestBody JoinRequest joinRequest)
        throws ResourceNotFoundException{
        joinRequestService.save(joinRequest);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }


    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<JoinRequest>> getJoinRequestByType(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity(joinRequestService.get(userId), HttpStatus.OK);
    }

    private Set<JoinRequestStatus> getStatuses(Set<String> statuses) {
        Set<JoinRequestStatus> statusSet = new HashSet<>();

        if (CollectionUtils.isEmpty(statuses)) {
            return Collections.EMPTY_SET;
        }

        for (String s: statuses) {
            // TODO possible alternation to Jackson settings hopefully its just the unit test
            s = s.replaceAll("\"", "");
            s = s.replaceAll("\\]", "");
            s = s.replaceAll("\\[", "");

            if (!StringUtils.isEmpty(s)) {
                JoinRequestStatus joinRequestStatus = JoinRequestStatus.valueOf(s);
                statusSet.add(joinRequestStatus);
            }

        }

        return statusSet;
    }


}
