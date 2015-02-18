package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.JoinRequestService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
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
 * RESTful interface for managing JoinRequests, where members of the public who are not currently Users in PatientView
 * can apply to join.
 *
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class JoinRequestController extends BaseController<JoinRequestController> {

    @Inject
    private JoinRequestService joinRequestService;

    /**
     * Publicly available method to submit a new JoinRequest.
     * @param joinRequest JointRequest to submit
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/public/joinrequest", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody JoinRequest joinRequest)
            throws ResourceNotFoundException, ResourceForbiddenException {
        joinRequestService.add(joinRequest);
    }

    /*
    // used by migration
    @RequestMapping(value = "/migrate/joinrequests", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void migrateJoinRequests(@RequestBody List<JoinRequest> joinRequests) throws ResourceNotFoundException {
        joinRequestService.migrate(joinRequests);
    }
    */

    /**
     * Get a JoinRequest given ID.
     * @param joinRequestId ID of JoinRequest to get
     * @return JoinRequest object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/joinrequest/{joinRequestId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.JoinRequest> get(@PathVariable("joinRequestId") Long joinRequestId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(joinRequestService.get(joinRequestId), HttpStatus.OK);
    }

    /**
     * Get a Page of JoinRequests available for view by a User given a User ID (staff user).
     * @param userId ID of User to retrieve JoinRequest
     * @param getParameters GetParameters object with filters and pagination, page size, number etc
     * @return Page of JoinRequest
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.JoinRequest>> getByUser(@PathVariable("userId") Long userId
            , GetParameters getParameters) throws ResourceNotFoundException {
        return new ResponseEntity<>(joinRequestService.getByUser(userId, getParameters), HttpStatus.OK);
    }

    /**
     * Get a count of viewable submitted JoinRequests given a user ID (staff user).
     * @param userId ID of User to retrieve submitted JoinRequest count
     * @return Long containing number of viewable submitted JoinRequests
     */
    @RequestMapping(value = "/user/{userId}/joinrequests/count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BigInteger> getCount(@PathVariable("userId") Long userId) {
        try {
            return new ResponseEntity<>(joinRequestService.getCount(userId), HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get list of available JoinRequestStatus, used in UI.
     * @return List of JoinRequestStatus
     */
    @RequestMapping(value = "/joinrequest/statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<JoinRequestStatus> getStatuses() {
        return CollectionUtils.arrayToList(JoinRequestStatus.values());
    }

    /**
     * Save an updated JoinRequest, typically by staff members adding comments or changing the status.
     * @param joinRequest JointRequest to update
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/joinrequest", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void save(@RequestBody JoinRequest joinRequest) throws ResourceNotFoundException {
        joinRequestService.save(joinRequest);
    }
}
