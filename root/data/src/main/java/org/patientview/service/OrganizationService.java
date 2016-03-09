package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface OrganizationService {

    UUID add(Patientview data) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Add Organization to FHIR based on Group, returns UUID of created/updated Organization. Used by API importer.
     * @param group Group used to build Organization
     * @return UUID logical id of created/updated Organization
     * @throws FhirResourceException
     */
    UUID add(Group group) throws FhirResourceException;

    boolean groupWithCodeExists(String code);
}
