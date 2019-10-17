package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ImmunisationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Immunisation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for managing patient Immunisation recordings
 */
@RestController
@ExcludeFromApiDoc
public class ImmunisationController extends BaseController<ImmunisationController> {

    @Inject
    private ImmunisationService immunisationService;

    /**
     * Add a Immunisation entry for a User
     *
     * @param userId Long User ID of patient to add Immunisation record for
     * @param record Immunisation object
     * @throws ResourceNotFoundException
     */
    @PostMapping(value = "/user/{userId}/immunisations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Immunisation add(@PathVariable("userId") Long userId,
                            @RequestParam(required = false) Long adminId,
                            @RequestBody Immunisation record) throws ResourceNotFoundException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return immunisationService.add(userId, adminId, record);
    }

    /**
     * Get details of Immunisation objects
     *
     * @param userId an ID of patient to get Immunisation for
     * @return an Immunisation object
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/immunisations/{recordId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> get(@PathVariable("userId") Long userId,
                                 @PathVariable("recordId") Long recordId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(immunisationService.get(recordId, userId), HttpStatus.OK);
    }

    /**
     * Update a Immunisation
     *
     * @param userId an ID of User associated with Immunisation
     * @param record Immunisation object to update
     * @return an Immunisation object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @PutMapping(value = "/user/{userId}/immunisations/{recordId}")
    public ResponseEntity<?> update(@PathVariable("userId") Long userId,
                                    @PathVariable("recordId") Long recordId,
                                    @RequestParam(required = false) Long adminId,
                                    @RequestBody Immunisation record)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return new ResponseEntity<>(immunisationService.update(recordId, userId, adminId, record), HttpStatus.OK);
    }

    /**
     * Delete a Immunisation associated with a User
     *
     * @param userId   Long User ID of patient to delete Immunisation for
     * @param recordId an ID of Immunisation to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @DeleteMapping(value = "/user/{userId}/immunisations/{recordId}")
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("recordId") Long recordId,
                       @RequestParam(required = false) Long adminId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        immunisationService.delete(recordId, userId, adminId);
    }

    /**
     * Get a List of all a User's Immunisation objects
     *
     * @param userId Long User ID of patient to get Immunisation objects for
     * @return a List of Immunisation
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/immunisations")
    public ResponseEntity<?> get(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        return new ResponseEntity<>(immunisationService.getList(userId), HttpStatus.OK);
    }

}
