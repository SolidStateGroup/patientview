package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirPractitioner;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/11/2014
 */
public interface PractitionerService {

    List<UUID> getPractitionerLogicalUuidsByName(String name) throws FhirResourceException;

    UUID addPractitioner(FhirPractitioner fhirPractitioner) throws ResourceNotFoundException, FhirResourceException;
}
