package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

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

    /**
     * Delete DiagnosticReport from FHIR, binary data from patientview given a subjectId and date range
     * @param subjectId UUID of subject (patient's logical id)
     * @param fromDate Date to delete diagnostic reports from
     * @param toDate Date to delete diagnostic reports to
     * @return int number of DiagnosticReport deleted
     * @throws FhirResourceException
     */
    int deleteBySubjectIdAndDateRange(UUID subjectId, Date fromDate, Date toDate) throws FhirResourceException;
}
