package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.model.FhirLink;

import java.util.List;
import java.util.UUID;

/**
 * Letter service for managing allergy data stored in FHIR as AllergyIntolerance, Substance, AdverseReaction.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/06/2015
 */
public interface AllergyService {

    void addAllergy(FhirAllergy fhirAllergy, FhirLink fhirLink) throws FhirResourceException;

    List<FhirAllergy> getBySubject(UUID subjectUuid) throws FhirResourceException;
}
