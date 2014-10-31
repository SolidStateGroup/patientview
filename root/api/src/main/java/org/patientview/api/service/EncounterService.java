package org.patientview.api.service;

import org.hl7.fhir.instance.model.Encounter;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface EncounterService {

    List<FhirEncounter> get(Long userId, String code) throws ResourceNotFoundException, FhirResourceException;

    List<Encounter> get(UUID patientUuid) throws FhirResourceException;

    void addEncounter(FhirEncounter fhirEncounter, FhirLink fhirLink, UUID organizationUuid)
            throws ResourceNotFoundException, FhirResourceException;
}
