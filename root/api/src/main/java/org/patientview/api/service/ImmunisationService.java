package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;

/**
 * Immunisation service to handle patient's immunisation recordings
 */
public interface ImmunisationService {

    /**
     * Add an Immunisation entry for a patient User
     *
     * @param userId  Long User ID of patient to add Immunisation for
     * @param adminId ID of admin User(viewing patient) or patient User
     * @param record  Immunisation object
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Immunisation add(Long userId, Long adminId, Immunisation record) throws ResourceNotFoundException,
            ResourceInvalidException;

    /**
     * Get an Immunisation object for patient
     *
     * @param userId   an ID of patient to Immunisation object for
     * @param recordId an ID of Immunisation to find
     * @return an Immunisation
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Immunisation get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Update a Immunisation
     *
     * @param recordId an ID of Immunisation to find
     * @param userId   an ID of User associated with Immunisation
     * @param adminId  ID of admin User(viewing patient) or patient User
     * @param record   Immunisation object to update
     * @return Immunisation object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    Immunisation update(Long userId, Long recordId, Long adminId, Immunisation record)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException;

    /**
     * Delete a Immunisation associated with a User
     *
     * @param adminId  ID of admin User(viewing patient) or patient User
     * @param userId   an ID of patient to delete Immunisation for
     * @param recordId an ID of Immunisation to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    void delete(Long userId, Long recordId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of all a User's Immunisation objects
     *
     * @param userId an ID of patient to get Immunisation objects for
     * @return List of Immunisation objects
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = {RoleName.PATIENT})
    List<Immunisation> getList(Long userId) throws ResourceNotFoundException;

    /**
     * Get a List of all a User's Immunisation objects.
     * No Roles check as used internally by job.
     *
     * @param userId an ID of patient to get Immunisation objects for
     * @return List of Immunisation objects
     * @throws ResourceNotFoundException
     */
    List<Immunisation> getListByPatient(Long userId);

    /**
     * Remove all Immunisation entries associated with a User.
     *
     * @param user User to delete Immunisation entries for
     */
    void deleteRecordsForUser(User user);

}
