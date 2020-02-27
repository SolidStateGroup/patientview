package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.RelapseMedication;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;

import java.util.List;

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
    @RoleOnly(roles = {RoleName.PATIENT})
    InsDiaryRecord add(Long userId, Long adminId, InsDiaryRecord record) throws ResourceNotFoundException,
            ResourceInvalidException, FhirResourceException;

    /**
     * Get an InsDiaryRecord object for patient
     *
     * @param userId   an ID of patient to get Diary object for
     * @param recordId an ID of InsDiaryRecord to find
     * @return an InsDiaryRecord
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    InsDiaryRecord get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Update a InsDiaryRecord
     *
     * @param userId  an ID of User associated with InsDiaryRecord
     * @param adminId ID of admin User(viewing patient) or patient User
     * @param record  InsDiaryRecord object to update
     * @return InsDiaryRecord object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    InsDiaryRecord update(Long userId, Long adminId, InsDiaryRecord record) throws ResourceNotFoundException,
            ResourceForbiddenException, ResourceInvalidException;

    /**
     * Delete a InsDiaryRecord associated with a User
     *
     * @param userId   an ID of patient to delete InsDiaryRecord for
     * @param recordId an ID of InsDiaryRecord to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
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
    @RoleOnly(roles = {RoleName.PATIENT})
    Page<InsDiaryRecord> findByUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException;

    /**
     * Get a list of InsDiaryRecord objects for a patient user.
     * No role checks as ised internally by job.
     *
     * @param userId an ID of patient to get InsDiaryRecord objects for
     * @return List of InsDiaryRecord objects
     * @throws ResourceNotFoundException
     */
    List<InsDiaryRecord> getListByUser(Long userId);

    /**
     * Add an RelapseMedication entry for a patient User
     *
     * @param userId     Long User ID of patient to add medication record for
     * @param relapseId  an id of Relapse object to add medication for
     * @param medication RelapseMedication object containing medication information
     * @throws ResourceNotFoundException
     * @throws ResourceInvalidException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    RelapseMedication addRelapseMedication(Long userId, Long relapseId, RelapseMedication medication)
            throws ResourceNotFoundException, ResourceInvalidException, ResourceForbiddenException;

    /**
     * Remove RelapseMedication record from Relapse
     *
     * @param userId       Long User ID of patient to delete medication record from
     * @param relapseId    an id of Relapse object to delete medication from
     * @param medicationId an id of RelapseMedication to remove
     * @throws ResourceNotFoundException
     * @throws ResourceInvalidException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    void deleteRelapseMedication(Long userId, Long relapseId, Long medicationId)
            throws ResourceNotFoundException, ResourceInvalidException, ResourceForbiddenException;


    /**
     * Hard delete all InsDiaryRecord entries associated with a User.
     *
     * Used internally when hard deleting patient from the system
     *
     * @param user User to delete InsDiaryRecord entries for
     */
    void deleteInsDiaryRecordsForUser(User user);

    /**
     * Hard delete all Relapse records associated with a User. This will also delete
     * associated RelapseMedication records.
     *
     * Used internally when hard deleting patient from the system.
     *
     * @param user User to delete InsDiaryRecord entries for
     */
    void deleteRelapseRecordsForUser(User user);

}
