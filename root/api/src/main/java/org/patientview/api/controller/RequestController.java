package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.RequestService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.enums.RequestStatus;
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
 * RESTful interface for managing Requests, where members of the public who are not currently Users in PatientView
 * can apply to join (join requests) or users have forgotten their password and login details (forgotten credentials).
 *
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class RequestController extends BaseController<RequestController> {

    @Inject
    private RequestService requestService;

    /**
     * Publicly available method to submit a new Request.
     * @param request JointRequest to submit
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/public/request", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody Request request)
            throws ResourceNotFoundException, ResourceForbiddenException {
        requestService.add(request);
    }

    /**
     * Complete SUBMITTED requests. Completes join requests where a user already exists (and user was created after
     * the request came in). Completes forgot login requests where a user has since logged in. Can only be called by
     * global admins.
     */
    @RequestMapping(value = "/request/complete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Integer> completeRequests() {
        return new ResponseEntity<>(requestService.completeRequests(), HttpStatus.OK);
    }

    /**
     * Get a Request given ID.
     * @param requestId ID of Request to get
     * @return Request object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/request/{requestId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.Request> get(@PathVariable("requestId") Long requestId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(requestService.get(requestId), HttpStatus.OK);
    }

    /**
     * Get a Page of Requests available for view by a User given a User ID (staff user).
     * @param userId ID of User to retrieve Requests
     * @param getParameters GetParameters object with filters and pagination, page size, number etc
     * @return Page of Request
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/requests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.Request>> getByUser(@PathVariable("userId") Long userId
            , GetParameters getParameters) throws ResourceNotFoundException {
        return new ResponseEntity<>(requestService.getByUser(userId, getParameters), HttpStatus.OK);
    }

    /**
     * Get a count of viewable submitted Requests given a user ID (staff user).
     * @param userId ID of User to retrieve submitted Request count
     * @return Long containing number of viewable submitted Requests
     */
    @RequestMapping(value = "/user/{userId}/requests/count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BigInteger> getCount(@PathVariable("userId") Long userId) {
        try {
            return new ResponseEntity<>(requestService.getCount(userId), HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get list of available RequestStatus, used in UI.
     * @return List of RequestStatus
     */
    @RequestMapping(value = "/request/statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<RequestStatus> getStatuses() {
        return CollectionUtils.arrayToList(RequestStatus.values());
    }

    /**
     * Save an updated Request, typically by staff members adding comments or changing the status.
     * @param request Request to update
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/request", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void save(@RequestBody Request request) throws ResourceNotFoundException {
        requestService.save(request);
    }
}
