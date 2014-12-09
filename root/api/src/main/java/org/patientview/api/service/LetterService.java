package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public interface LetterService {

    @UserOnly
    List<FhirDocumentReference> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;

    void addLetter(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException;

    @UserOnly
    void delete(Long userId, Long groupId, Long date) throws ResourceNotFoundException, FhirResourceException;
}
