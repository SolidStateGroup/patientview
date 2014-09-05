package org.patientview.importer.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientService {
    UUID add(Patientview patient, ResourceReference practitionerReference)
            throws FhirResourceException, ResourceNotFoundException;
}
