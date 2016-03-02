package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FileData;

import java.util.List;

/**
 * Diagnostic service, for retrieving patient diagnostics from FHIR
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
public interface ApiDiagnosticService {

    /**
     * Get Diagnostics for a patient from FHIR given a User ID.
     * @param userId ID of User to get Diagnostics for
     * @return List of diagnostic reports, retrieved from FHIR
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<FhirDiagnosticReport> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;

    FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException;
}
