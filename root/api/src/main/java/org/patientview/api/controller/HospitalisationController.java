package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.HospitalisationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
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
 * RESTful interface for managing patient Hospitalisation recordings
 */
@RestController
@ExcludeFromApiDoc
public class HospitalisationController extends BaseController<HospitalisationController> {

    @Inject
    private HospitalisationService hospitalisationService;

    /**
     * Add a Hospitalisation entry for a User
     *
     * @param userId Long User ID of patient to add Hospitalisation record for
     * @param record Hospitalisation object
     * @throws ResourceNotFoundException
     */
    @PostMapping(value = "/user/{userId}/hospitalisations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Hospitalisation add(@PathVariable("userId") Long userId,
                               @RequestParam(required = false) Long adminId,
                               @RequestBody Hospitalisation record)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return hospitalisationService.add(userId, adminId, record);
    }

    /**
     * Get details of Hospitalisation objects
     *
     * @param userId an ID of patient to get Hospitalisation for
     * @return an Hospitalisation object
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/hospitalisations/{recordId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> get(@PathVariable("userId") Long userId,
                                 @PathVariable("recordId") Long recordId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(hospitalisationService.get(recordId, userId), HttpStatus.OK);
    }

    /**
     * Update a Hospitalisation
     *
     * @param userId an ID of User associated with Hospitalisation
     * @param record Hospitalisation object to update
     * @return an Hospitalisation object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @PutMapping(value = "/user/{userId}/hospitalisations/{recordId}")
    public ResponseEntity<?> update(@PathVariable("userId") Long userId,
                                    @PathVariable("recordId") Long recordId,
                                    @RequestParam(required = false) Long adminId,
                                    @RequestBody Hospitalisation record)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return new ResponseEntity<>(hospitalisationService.update(recordId, userId, adminId, record), HttpStatus.OK);
    }

    /**
     * Delete a Hospitalisation associated with a User
     *
     * @param userId   Long User ID of patient to delete Hospitalisation for
     * @param recordId an ID of Hospitalisation to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @DeleteMapping(value = "/user/{userId}/hospitalisations/{recordId}")
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("recordId") Long recordId,
                       @RequestParam(required = false) Long adminId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        hospitalisationService.delete(recordId, userId, adminId);
    }

    /**
     * Get a List of all a User's Hospitalisation objects
     *
     * @param userId Long User ID of patient to get Hospitalisation objects for
     * @return a List of Hospitalisation
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/hospitalisations")
    public ResponseEntity<?> get(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        return new ResponseEntity<>(hospitalisationService.getList(userId), HttpStatus.OK);
    }
}
