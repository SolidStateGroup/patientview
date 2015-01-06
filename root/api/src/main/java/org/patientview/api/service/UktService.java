package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UktService {

    void importData() throws ResourceNotFoundException, FhirResourceException, UktException;

    void exportData() throws ResourceNotFoundException, FhirResourceException, UktException;
}
