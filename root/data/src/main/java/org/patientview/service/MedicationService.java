package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

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
    void addMedicationStatement(
            org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement, FhirLink fhirLink)
            throws FhirResourceException;

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;
}
