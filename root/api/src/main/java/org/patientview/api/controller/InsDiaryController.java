package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.InsDiaryRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for managing patient INS Diary recordings
 */
@RestController
@ExcludeFromApiDoc
public class InsDiaryController extends BaseController<InsDiaryController> {

    @Inject
    private InsDiaryService insDiaryService;

    /**
     * Add a Food Diary entry for a User
     *
     * @param userId Long User ID of patient to add INS Diary record for
     * @param record InsDiaryRecord object containing diary information
     * @throws ResourceNotFoundException
     */
    @PostMapping(value = "/user/{userId}/insdiary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@PathVariable("userId") Long userId, @RequestBody InsDiaryRecord record)
            throws ResourceNotFoundException {
        insDiaryService.add(userId, record);
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
     * Update a FoodDiary
     *
     * @param userId an ID of User associated with InsDiaryRecord
     * @param record InsDiaryRecord object to update
     * @return an InsDiaryRecord object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @PutMapping(value = "/user/{userId}/insdiary")
    public ResponseEntity<?> update(@PathVariable("userId") Long userId, @RequestBody InsDiaryRecord record)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(insDiaryService.update(userId, record), HttpStatus.OK);
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
     * Get a List of all a User's InsDiaryRecord objects
     *
     * @param userId Long User ID of patient to get Food Diary objects for
     * @return a List of InsDiaryRecord
     * @throws ResourceNotFoundException
     */
    @GetMapping(value = "/user/{userId}/insdiary")
    public ResponseEntity<?> get(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        return new ResponseEntity<>(insDiaryService.getList(userId), HttpStatus.OK);
    }

}
