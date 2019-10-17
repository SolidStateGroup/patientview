package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
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
    Hospitalisation add(Long userId, Long adminId, Hospitalisation record) throws ResourceNotFoundException;

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
    Hospitalisation get(Long recordId, Long userId) throws ResourceNotFoundException, ResourceForbiddenException;


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
    Hospitalisation update(Long recordId, Long userId, Long adminId, Hospitalisation record)
            throws ResourceNotFoundException, ResourceForbiddenException;

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
    void delete(Long recordId, Long userId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException;

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


}
