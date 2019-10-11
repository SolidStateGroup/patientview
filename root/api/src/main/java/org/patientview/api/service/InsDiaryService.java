package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.InsDiaryRecord;

import java.util.List;

/**
 * Ins Diary service to handle patient's INS diary recordings
 */
public interface InsDiaryService {

    /**
     * Add an InsDiaryRecord entry for a patient User
     *
     * @param userId Long User ID of patient to add InsDiaryRecord for
     * @param record InsDiaryRecord object containing diary information
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void add(Long userId, InsDiaryRecord record) throws ResourceNotFoundException;

    /**
     * Get an InsDiaryRecord object for patient
     *
     * @param userId   an ID of patient to get Diary object for
     * @param recordId an ID of InsDiaryRecord to find
     * @return an InsDiaryRecord
     * @throws ResourceNotFoundException
     */
    @UserOnly
    InsDiaryRecord get(Long userId, Long recordId) throws ResourceNotFoundException;


    /**
     * Update a InsDiaryRecord
     *
     * @param userId an ID of User associated with InsDiaryRecord
     * @param record InsDiaryRecord object to update
     * @return InsDiaryRecord object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    InsDiaryRecord update(Long userId, InsDiaryRecord record) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a InsDiaryRecord associated with a User
     *
     * @param userId   an ID of patient to delete InsDiaryRecord for
     * @param recordId an ID of InsDiaryRecord to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void delete(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of all a User's InsDiaryRecord objects
     *
     * @param userId an ID of patient to get InsDiaryRecord objects for
     * @return List of InsDiaryRecord objects
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<InsDiaryRecord> getList(Long userId) throws ResourceNotFoundException;


}
