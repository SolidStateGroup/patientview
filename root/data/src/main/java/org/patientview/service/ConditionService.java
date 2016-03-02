package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConditionService {

    /**
     * Get a list of FHIR Conditions given a UUID from FhirLink representing the patient in FHIR.
     * @param patientUuid UUID representing the patient in FHIR
     * @return List of FHIR Conditions
     * @throws FhirResourceException
     */
    List<Condition> get(UUID patientUuid) throws FhirResourceException;

    // used by migration
    void add(FhirCondition fhirCondition, FhirLink fhirLink) throws ResourceNotFoundException, FhirResourceException;

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;
}
