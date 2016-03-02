package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientService {

    FhirLink add(Patientview patient, ResourceReference practitionerReference)
            throws FhirResourceException, ResourceNotFoundException;

    Identifier matchPatientByIdentifierValue(Patientview patientview) throws ResourceNotFoundException;
}
