package org.patientview.api.service;

import org.hl7.fhir.instance.model.Encounter;
import org.patientview.api.model.FhirEncounter;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface EncounterService {

    List<FhirEncounter> get(Long userId, String code) throws ResourceNotFoundException, FhirResourceException;

    List<Encounter> get(UUID patientUuid) throws FhirResourceException ;

}
