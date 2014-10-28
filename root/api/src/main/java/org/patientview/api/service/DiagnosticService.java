package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
public interface DiagnosticService {

    @UserOnly
    List<FhirDiagnosticReport> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;
}
