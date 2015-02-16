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
 * RESTful interface for managing User Identifiers (NHS/CHI number etc), including validation. Identifiers must be 
 * unique and are currently only used for patient Users. 
 *
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@RestController
public class IdentifierController extends BaseController<IdentifierController> {

    @Inject
    private IdentifierService identifierService;

    /**
     * Add an Identifier to a User, checking if not already in use by another User 
     * @param userId ID of User to add Identifier to
     * @param identifier String Identifier to add
     * @return Identifier object, newly created (Note: consider returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/user/{userId}/identifiers", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Identifier> add(@PathVariable("userId") Long userId, @RequestBody Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        return new ResponseEntity<>(identifierService.add(userId, identifier), HttpStatus.CREATED);
    }

    /**
     * Delete an Identifier given an ID 
     * @param identifierId ID of Identifier to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        identifierService.delete(identifierId);
    }

    /**
     * Get an Identifier given and ID
     * @param identifierId ID of Identifier to retrieve
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.GET)
    @ResponseBody
    public void get(@PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        identifierService.get(identifierId);
    }

    /**
     * Save an updated Identifier 
     * @param identifier Identifier to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/identifier", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        identifierService.save(identifier);
    }

    /**
     * Validate an Identifier, e.g. NHS Number must be within certain range
     * @param userIdentifier UserIdentifier object containing required information to validate Identifier value
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     * @throws ResourceInvalidException
     */
    @RequestMapping(value = "/identifier/validate", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void validateNoUser(@RequestBody UserIdentifier userIdentifier)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException, ResourceInvalidException {
        identifierService.validate(userIdentifier);
    }
}
