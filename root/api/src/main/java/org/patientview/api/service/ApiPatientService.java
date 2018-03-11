package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;
import java.util.UUID;

/**
 * Patient service, for managing the patient records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ApiPatientService {

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     * @param userId ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<org.patientview.api.model.Patient> get(Long userId, List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a FHIR Patient record given the UUID associated with the Patient in FHIR.
     * @param uuid UUID of Patient in FHIR to retrieve
     * @return FHIR Patient
     * @throws FhirResourceException
     */
    Patient get(UUID uuid) throws FhirResourceException;

    /**
     * Internal method used to get required fields for a research study
     * Encounters are returned as db encounters
     *
     * @param userId
     * @return FHIR Patient
     * @throws FhirResourceException
     */
    List<org.patientview.api.model.Patient> getPatientResearchStudyCriteria(Long userId)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a list of basic patient information. Used by CKD.
     * @param userId ID of User to retrieve patient record for
     * @return List of Patient objects containing patient information
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<org.patientview.api.model.Patient> getBasic(Long userId)
        throws FhirResourceException, ResourceNotFoundException;

    /**
     * Update or create patient details (used by API importer)
     * @param fhirPatient details to update
     * @return ServerResponse with success/error messages
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importPatient(FhirPatient fhirPatient);

    // API
    @RoleOnly(roles = { RoleName.UNIT_ADMIN_API })
    void update(Long userId, Long groupId, FhirPatient fhirPatient)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;
}
