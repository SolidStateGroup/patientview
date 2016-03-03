package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientService {

    FhirLink add(Patientview patient, ResourceReference practitionerReference)
            throws FhirResourceException, ResourceNotFoundException;

    /**
     * Build a FHIR Patient, used when entering own results if no current link between PatientView and FHIR.
     * @param user User to build FHIR Patient for
     * @param identifier Identifier associated with User and to be assigned to new FHIR Patient
     * @return FHIR Patient
     */
    Patient buildPatient(User user, Identifier identifier);

    /**
     * Delete all non Observation Patient data stored in Fhir given a Set of FhirLink.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException;

    Identifier matchPatientByIdentifierValue(Patientview patientview) throws ResourceNotFoundException;
}
