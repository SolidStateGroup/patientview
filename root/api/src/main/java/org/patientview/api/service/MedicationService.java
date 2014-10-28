package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public interface MedicationService {

    @UserOnly
    List<FhirMedicationStatement> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;
}
