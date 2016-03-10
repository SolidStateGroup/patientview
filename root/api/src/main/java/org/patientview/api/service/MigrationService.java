package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MigrationUser;

import javax.persistence.EntityExistsException;

/**
 * Migration service, used in one-off operation to transfer information from PatientView 1 to PatientView 2.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/11/2014
 */
public interface MigrationService {

    @RoleOnly
    Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;

    @RoleOnly
    void migrateObservations(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;

    @RoleOnly
    void migrateObservationsFast();

    @RoleOnly
    Long migrateUserExisting(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;

    void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;

    void migrateTestObservations(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException;
}
