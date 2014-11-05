package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface PatientService {

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<org.patientview.api.model.Patient> get(Long userId, List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException;

    Patient get(UUID uuid) throws FhirResourceException;

    Patient buildPatient(User user, Identifier identifier);

    // migration only
    void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;

    // migration only
    void migrateObservations(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;

    void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException;

    void deleteExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException;
}
