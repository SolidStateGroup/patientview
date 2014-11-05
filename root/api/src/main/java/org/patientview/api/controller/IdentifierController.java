package org.patientview.api.controller;

import org.patientview.api.model.UserIdentifier;
import org.patientview.api.service.IdentifierService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
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
import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@RestController
public class IdentifierController extends BaseController<IdentifierController> {

    @Inject
    private IdentifierService identifierService;

    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        identifierService.delete(identifierId);
    }

    @RequestMapping(value = "/identifier", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        identifierService.save(identifier);
    }

    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.GET)
    @ResponseBody
    public void get(@PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        identifierService.get(identifierId);
    }

    @RequestMapping(value = "/user/{userId}/identifiers", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Identifier> add(@PathVariable("userId") Long userId, @RequestBody Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        return new ResponseEntity<>(identifierService.add(userId, identifier), HttpStatus.CREATED);
    }

    // no longer used 5/11/14
    /*@RequestMapping(value = "/identifier/value/{identifierValue}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Identifier> getIdentifierByValue(@PathVariable("identifierValue") String identifierValue)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(identifierService.getIdentifierByValue(identifierValue), HttpStatus.OK);
    }*/

    @RequestMapping(value = "/identifier/validate", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void validateNoUser(@RequestBody UserIdentifier userIdentifier)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException, ResourceInvalidException {
        identifierService.validate(userIdentifier);
    }
}
