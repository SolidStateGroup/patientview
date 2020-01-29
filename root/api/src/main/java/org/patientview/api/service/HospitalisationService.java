package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;

/**
 * HospitalisationService service to handle patient's hospitalisation recordings
 */
public interface HospitalisationService {

    /**
     * Add an Hospitalisation entry for a patient User
     *
     * @param userId Long User ID of patient to add Hospitalisation for
     * @param record Hospitalisation object containing diary information
     * @return an Hospitalisation
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Hospitalisation add(Long userId, Long adminId, Hospitalisation record) throws ResourceNotFoundException,
            ResourceForbiddenException, ResourceInvalidException;

    /**
     * Get an Hospitalisation object for patient
     *
     * @param userId   an ID of patient to get Hospitalisation object for
     * @param recordId an ID of Hospitalisation to find
     * @return an Hospitalisation
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Hospitalisation get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Update a Hospitalisation
     *
     * @param recordId an ID of Hospitalisation to find
     * @param userId   an ID of User associated with Hospitalisation
     * @param adminId  ID of admin User(viewing patient) or patient User
     * @param record   Hospitalisation object to update
     * @return Hospitalisation object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Hospitalisation update(Long userId, Long recordId, Long adminId, Hospitalisation record)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException;

    /**
     * Delete a Hospitalisation associated with a User
     *
     * @param userId   an ID of patient to delete Hospitalisation for
     * @param adminId  ID of admin User(viewing patient) or patient User
     * @param recordId an ID of Hospitalisation to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    void delete(Long userId, Long recordId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of all a User's Hospitalisation objects
     *
     * @param userId an ID of patient to get Hospitalisation objects for
     * @return List of Hospitalisation objects
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    List<Hospitalisation> getList(Long userId) throws ResourceNotFoundException;

    /**
     * Get a List of all a User's Hospitalisation objects.
     * No Roles check as used internally by job.
     *
     * @param userId an ID of patient to get Hospitalisation objects for
     * @return List of Hospitalisation objects
     * @throws ResourceNotFoundException
     */
    List<Hospitalisation> getListByPatient(Long userId) throws ResourceNotFoundException;


    /**
     * Remove all Hospitalisation entries associated with a User.
     *
     * @param user User to delete Hospitalisation entries for
     */
    void deleteRecordsForUser(User user);

}
