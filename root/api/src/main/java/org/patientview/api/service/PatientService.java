package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface PatientService {

    List<org.patientview.api.model.Patient> get(Long userId) throws FhirResourceException, ResourceNotFoundException;

    Patient get(UUID uuid) throws FhirResourceException;

}
