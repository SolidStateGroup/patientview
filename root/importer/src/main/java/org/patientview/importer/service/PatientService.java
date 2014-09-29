package org.patientview.importer.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientService {

    UUID add(Patientview patient, ResourceReference practitionerReference)
            throws FhirResourceException, ResourceNotFoundException;

    public List<FhirLink> getInactivePatientFhirLinksByGroup(Patientview patientview) throws ResourceNotFoundException;

    public void deleteByResourceId(UUID resourceId) throws FhirResourceException, SQLException;

    public void deleteFhirLink(FhirLink fhirlink) throws ResourceNotFoundException;

    public Identifier matchPatientByIdentifierValue(Patientview patientview) throws ResourceNotFoundException;

}
