package org.patientview.importer.manager;

import generated.Patientview;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ImportManager {

    // public so can be tested
    void createGpLetter(FhirLink fhirLink, Patientview patientview) throws ResourceNotFoundException;

    void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException;

    void validate(Patientview patientview) throws ImportResourceException;
}
