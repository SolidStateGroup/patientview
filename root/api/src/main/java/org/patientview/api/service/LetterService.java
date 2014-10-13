package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirLetter;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public interface LetterService {

    @UserOnly
    List<FhirLetter> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;
}
