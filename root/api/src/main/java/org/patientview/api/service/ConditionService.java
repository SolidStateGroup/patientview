package org.patientview.api.service;

import org.hl7.fhir.instance.model.Condition;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface ConditionService {

    List<Condition> get(Long userId, String code) throws ResourceNotFoundException, FhirResourceException;

    List<Condition> get(UUID patientUuid) throws FhirResourceException;

}
