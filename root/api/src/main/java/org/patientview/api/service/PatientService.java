package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Patient service, for managing the patient records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface PatientService {

    /**
     * Build a FHIR Patient, used when entering own results if no current link between PatientView and FHIR.
     * @param user User to build FHIR Patient for
     * @param identifier Identifier associated with User and to be assigned to new FHIR Patient
     * @return FHIR Patient
     */
    Patient buildPatient(User user, Identifier identifier);

    /**
     * Delete all Observations from FHIR given a Set of FhirLink, used when deleting a patient and in migration.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    void deleteAllExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException;

    /**
     * Delete all non Observation Patient data stored in Fhir given a Set of FhirLink.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException;

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     * @param userId ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<org.patientview.api.model.Patient> get(Long userId, List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException;

    /**
     * Get a FHIR Patient record given the UUID associated with the Patient in FHIR.
     * @param uuid UUID of Patient in FHIR to retrieve
     * @return FHIR Patient
     * @throws FhirResourceException
     */
    Patient get(UUID uuid) throws FhirResourceException;

    // migration only
    void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;

    // migration only
    void migrateTestObservations(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;

    // API
    @RoleOnly(roles = { RoleName.UNIT_ADMIN_API })
    void update(Long userId, Long groupId, FhirPatient fhirPatient)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;
}
