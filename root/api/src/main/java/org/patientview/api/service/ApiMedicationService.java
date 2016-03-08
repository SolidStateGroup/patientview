package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirMedicationStatementRange;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;

/**
 * Medication service, for storing and retrieving FHIR medication data.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public interface ApiMedicationService {

    /**
     * Get medication data from FHIR given a User ID
     * @param userId ID of user to get medication for
     * @return List of FhirMedicationStatement suitable for display in UI
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<FhirMedicationStatement> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get medication data from FHIR given a User ID and by date
     * @param userId ID of user to get medication for
     * @return List of FhirMedicationStatement suitable for display in UI
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<FhirMedicationStatement> getByUserId(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Given a FhirMedicationStatementRange object with a start, end date and list of medication, store in FHIR
     * @param fhirMedicationStatementRange FhirMedicationStatementRange containing start date, end date and medication to import
     * @return ServerResponse object containing success, error message and successful status
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importMedication(FhirMedicationStatementRange fhirMedicationStatementRange);
}
