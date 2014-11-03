package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
public interface DiagnosticService {

    @UserOnly
    List<FhirDiagnosticReport> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;

    void addDiagnosticReport(
            org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport, FhirLink fhirLink)
            throws FhirResourceException;
}
