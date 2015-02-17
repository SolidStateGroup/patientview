package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ContactPointService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for the basic Crud operation for ContactPoints, a property of Groups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class ContactPointController extends BaseController<ContactPointController> {

    @Inject
    private ContactPointService contactPointService;

    /**
     * Add a new ContactPoint to a Group.
     * @param groupId ID of Group to add ContactPoint to
     * @param contactPoint ContactPoint object containing all required properties
     * @return ContactPoint, newly created (consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}/contactpoints", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ContactPoint> add(@PathVariable("groupId") Long groupId,
                                            @RequestBody ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(contactPointService.add(groupId, contactPoint), HttpStatus.CREATED);
    }

    /**
     * Delete a ContactPoint given an ID.
     * @param contactPointId ID of ContactPoint to delete.
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/contactpoint/{contactPointId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("contactPointId") Long contactPointId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        contactPointService.delete(contactPointId);
    }

    // used by migration
    @RequestMapping(value = "/contactpoint/type/{type}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ContactPointType> getContactPointType(@PathVariable(value = "type") String type)
            throws ResourceInvalidException {
        return new ResponseEntity<>(contactPointService.getContactPointType(type), HttpStatus.OK);
    }

    /**
     * Save an updated ContactPoint.
     * @param contactPoint ContactPoint object to save
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/contactpoint", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {
        contactPointService.save(contactPoint);
    }
}
