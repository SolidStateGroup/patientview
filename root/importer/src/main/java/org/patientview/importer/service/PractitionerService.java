package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PractitionerService {

    public UUID add(Patientview data) throws FhirResourceException;
}
