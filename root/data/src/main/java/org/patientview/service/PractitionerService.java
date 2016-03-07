package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPractitioner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PractitionerService {

    UUID add(Patientview data) throws FhirResourceException;

    void addOtherPractitionersToPatient(Patientview data, FhirLink fhirLink) throws FhirResourceException;

    List<UUID> getPractitionerLogicalUuidsByName(String name) throws FhirResourceException;

    // used by migration & importer
    UUID add(FhirPractitioner fhirPractitioner) throws FhirResourceException;
}
