package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirPractitioner;

import java.util.List;
import java.util.UUID;

/**
 * Practitioner service, for managing Practitioners (GPs) within FHIR, used during migration.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/11/2014
 */
public interface PractitionerService {

    // used by migration
    List<UUID> getPractitionerLogicalUuidsByName(String name) throws FhirResourceException;

    // used by migration
    UUID addPractitioner(FhirPractitioner fhirPractitioner) throws ResourceNotFoundException, FhirResourceException;
}
