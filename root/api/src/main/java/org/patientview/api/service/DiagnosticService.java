package org.patientview.api.service;

import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
public interface DiagnosticService {

    List<FhirDiagnosticReport> getByUserId(Long userId)
            throws ResourceNotFoundException, FhirResourceException;
}
