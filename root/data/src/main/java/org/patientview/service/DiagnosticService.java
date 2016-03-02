package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface DiagnosticService {

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;

    // used by migration
    void add(org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport, FhirLink fhirLink)
            throws FhirResourceException;
}
