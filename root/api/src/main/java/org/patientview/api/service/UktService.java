package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing import and export of UKT transplant status data from/to text files and FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UktService {

    /**
     * Import UKT transplant status data from text file and store in FHIR.
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws UktException
     */
    void importData() throws ResourceNotFoundException, FhirResourceException, UktException;

    /**
     * Export UKT data to text file from FHIR.
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws UktException
     */
    void exportData() throws ResourceNotFoundException, FhirResourceException, UktException;
}
