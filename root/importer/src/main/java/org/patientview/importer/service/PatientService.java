package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.FhirResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientService {
    void add(Patientview patient) throws FhirResourceException, ResourceNotFoundException;
}
