package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDiagnosticReportRange;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

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

    /**
     * Given a FhirDiagnosticReportRange object with a start, end date and list of diagnostics, store in FHIR
     * @param fhirDiagnosticReportRange FhirDiagnosticReportRange containing start date, end date and diagnostics
     *                                  to import
     * @return ServerResponse object containing success, error message and successful status
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importDiagnostics(FhirDiagnosticReportRange fhirDiagnosticReportRange);
}
