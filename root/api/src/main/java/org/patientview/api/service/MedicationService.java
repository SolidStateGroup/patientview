package org.patientview.api.service;

import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public interface MedicationService {

    List<FhirMedicationStatement> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;
}
