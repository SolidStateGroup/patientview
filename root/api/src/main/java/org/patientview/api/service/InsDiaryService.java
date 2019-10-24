package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.InsDiaryRecord;
import org.springframework.data.domain.Page;

/**
 * Ins Diary service to handle patient's INS diary recordings
 */
public interface InsDiaryService {

    /**
     * Add an InsDiaryRecord entry for a patient User
     *
     * @param userId  Long User ID of patient to add InsDiaryRecord for
     * @param adminId ID of admin User(viewing patient) or patient User
     * @param record  InsDiaryRecord object containing diary information
     * @throws ResourceNotFoundException
     */
    @UserOnly
    InsDiaryRecord add(Long userId, Long adminId, InsDiaryRecord record) throws ResourceNotFoundException, ResourceInvalidException, FhirResourceException;

    /**
     * Get an InsDiaryRecord object for patient
     *
     * @param userId   an ID of patient to get Diary object for
     * @param recordId an ID of InsDiaryRecord to find
     * @return an InsDiaryRecord
     * @throws ResourceNotFoundException
     */
    @UserOnly
    InsDiaryRecord get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException;


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
     * Get a Page of InsDiaryRecord objects for a patient user
     *
     * @param userId        an ID of patient to get InsDiaryRecord objects for
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return List of InsDiaryRecord objects
     * @throws ResourceNotFoundException
     */
    @UserOnly
    Page<InsDiaryRecord> findByUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException;


}
