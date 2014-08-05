package org.patientview.api.controller;

import org.patientview.api.exception.ResourceInvalidException;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.ContactPointService;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class ContactPointController {

    private final static Logger LOG = LoggerFactory.getLogger(ContactPointController.class);

    @Inject
    private ContactPointService contactPointService;


    @RequestMapping(value = "/contactpoint", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ContactPoint> createContactPoint(@RequestBody ContactPoint contactPoint,
                                           UriComponentsBuilder uriComponentsBuilder)
    throws ResourceNotFoundException {

        contactPoint = contactPointService.add(contactPoint);

        UriComponents uriComponents = uriComponentsBuilder.path("/contactpoint/{id}").buildAndExpand(contactPoint.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<ContactPoint>(contactPoint, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/contactpoint/{contactPointId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteContactPoint(@PathVariable("contactPointId") Long contactPointId) {
        contactPointService.delete(contactPointId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/contactpoint", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> saveContactPoint(@RequestBody ContactPoint contactPoint
            , UriComponentsBuilder uriComponentsBuilder) throws ResourceNotFoundException {
        ContactPoint updatedContactPoint = contactPointService.save(contactPoint);
        LOG.debug("Updated contactPoint with id " + updatedContactPoint.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/contactpoint/{contactPointId}").buildAndExpand(updatedContactPoint.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/contactpoint/type/{type}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ContactPointType> getContactPointType(@PathVariable(value = "type") String type)
            throws ResourceInvalidException {
        ContactPointType contactPointType = contactPointService.getContactPointType(type);
        LOG.debug("Getting contact point type " + type);
        return new ResponseEntity<> (contactPointType, HttpStatus.OK);
    }



}
