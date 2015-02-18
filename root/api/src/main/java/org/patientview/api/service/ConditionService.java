package org.patientview.api.service;

import org.hl7.fhir.instance.model.Condition;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;

import java.util.List;
import java.util.UUID;

/**
 * Condition service, to get patient Conditions from FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface ConditionService {

    /**
     * Get a list of FHIR Conditions given a UUID from FhirLink representing the patient in FHIR.
     * @param patientUuid UUID representing the patient in FHIR
     * @return List of FHIR Conditions
     * @throws FhirResourceException
     */
    List<Condition> get(UUID patientUuid) throws FhirResourceException;

    // used by migration
    void addCondition(FhirCondition fhirCondition, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException;
}
