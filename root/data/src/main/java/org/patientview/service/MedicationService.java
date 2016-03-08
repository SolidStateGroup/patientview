package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface MedicationService {

    /**
     * Used during migration, to create FHIR record MedicationStatement
     * @param fhirMedicationStatement Transport object to hold FHIR medication statement
     * @param fhirLink Link between user and FHIR patient record
     * @throws FhirResourceException
     */
    void add(org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement, FhirLink fhirLink)
            throws FhirResourceException;

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException;

    /**
     * Delete MedicationStatement and Medication from FHIR given a patientId and date range
     * @param patientId UUID of patient (logical id)
     * @param fromDate Date to delete medication from
     * @param toDate Date to delete medication to
     * @return int number of MedicationStatement deleted (usually equal to number of Medication deleted)
     * @throws FhirResourceException
     */
    int deleteBySubjectIdAndDateRange(UUID patientId, Date fromDate, Date toDate) throws FhirResourceException;
}
