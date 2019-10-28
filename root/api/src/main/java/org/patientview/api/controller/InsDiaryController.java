package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.RelapseMedication;
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
 * RESTful interface for managing patient INS Diary recordings.
 */
@RestController
@ExcludeFromApiDoc
public class InsDiaryController extends BaseController<InsDiaryController> {

    @Inject
    private InsDiaryService insDiaryService;

    /**
     * Add a Ins Diary entry for a User
     *
     * @param userId Long User ID of patient to add INS Diary record for
     * @param record InsDiaryRecord object containing diary information
     * @throws ResourceNotFoundException
     */
    @PostMapping(value = "/user/{userId}/insdiary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InsDiaryRecord add(@PathVariable("userId") Long userId,
                              @RequestParam(required = false) Long adminId,
                              @RequestBody InsDiaryRecord record)
            throws ResourceNotFoundException, ResourceInvalidException, FhirResourceException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return insDiaryService.add(userId, adminId, record);
    }

    /**
     * Get details of InsDiaryRecord objects
     *
     * @param userId an ID of patient to get InsDiaryRecord for
     * @return an InsDiaryRecord object
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/insdiary/{recordId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> get(@PathVariable("userId") Long userId, @PathVariable("recordId") Long recordId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(insDiaryService.get(userId, recordId), HttpStatus.OK);
    }

    /**
     * Update a InsDiary
     *
     * @param userId an ID of User associated with InsDiaryRecord
     * @param record InsDiaryRecord object to update
     * @return an InsDiaryRecord object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @PutMapping(value = "/user/{userId}/insdiary")
    public ResponseEntity<?> update(@PathVariable("userId") Long userId,
                                    @RequestParam(required = false) Long adminId,
                                    @RequestBody InsDiaryRecord record)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        return new ResponseEntity<>(insDiaryService.update(userId, adminId, record), HttpStatus.OK);
    }

    /**
     * Delete a InsDiaryRecord associated with a User
     *
     * @param userId   Long User ID of patient to delete InsDiaryRecord for
     * @param recordId an ID of InsDiaryRecord to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @DeleteMapping(value = "/user/{userId}/insdiary/{recordId}")
    public void delete(@PathVariable("userId") Long userId, @PathVariable("recordId") Long recordId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        insDiaryService.delete(userId, recordId);
    }

    /**
     * Get a Page of a User's InsDiaryRecord objects
     *
     * @param userId        Long User ID of patient to get Food Diary objects for
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return a List of InsDiaryRecord
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/insdiary")
    public ResponseEntity<?> get(@PathVariable("userId") Long userId,
                                 GetParameters getParameters) throws ResourceNotFoundException {
        return new ResponseEntity<>(insDiaryService.findByUser(userId, getParameters), HttpStatus.OK);
    }


    /**
     * Add a RelapseMedication for a User
     *
     * @param userId     Long User ID of patient to add medication record for
     * @param relapseId  an id of Relapse object to add medication for
     * @param medication RelapseMedication object containing medication information
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws ResourceInvalidException
     */
    @PostMapping(value = "/user/{userId}/relapses/{relapseId}/medications", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RelapseMedication addRelapseMedication(@PathVariable("userId") Long userId,
                                                  @PathVariable("relapseId") Long relapseId,
                                                  @RequestBody RelapseMedication medication)
            throws ResourceNotFoundException, ResourceInvalidException, ResourceForbiddenException {
        return insDiaryService.addRelapseMedication(userId, relapseId, medication);
    }

    /**
     * Delete a RelapseMedication record from Relapse
     *
     * @param userId       Long User ID of patient to delete medication record from
     * @param relapseId    an id of Relapse object to delete medication from
     * @param medicationId an id of RelapseMedication to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws ResourceInvalidException
     */
    @DeleteMapping(value = "/user/{userId}/relapses/{relapseId}/medications/{medicationId}")
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("recordId") Long relapseId,
                       @PathVariable("medicationId") Long medicationId)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        insDiaryService.deleteRelapseMedication(userId, relapseId, medicationId);
    }

}
