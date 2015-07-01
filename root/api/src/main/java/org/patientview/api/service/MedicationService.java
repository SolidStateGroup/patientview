package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;

import java.util.List;

/**
 * Medication service, for storing and retrieving FHIR medication data.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public interface MedicationService {

    /**
     * Used during migration, to create FHIR record MedicationStatement
     * @param fhirMedicationStatement Transport object to hold FHIR medication statement
     * @param fhirLink Link between user and FHIR patient record
     * @throws FhirResourceException
     */
    void addMedicationStatement(
            org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement, FhirLink fhirLink)
            throws FhirResourceException;

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
     * Get medication data from FHIR given a User ID by date
     * @param userId ID of user to get medication for
     * @return List of FhirMedicationStatement suitable for display in UI
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<FhirMedicationStatement> getByUserId(Long userId, String fromDate, String toDate) throws ResourceNotFoundException, FhirResourceException;
}
