package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;

import java.util.List;

/**
 * Letter service for managing letters stored in FHIR as DocumentReferences.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public interface LetterService {

    // used by migration
    void addLetter(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException;

    /**
     * Delete a letter based on User ID, Group ID (Group that imported the letter) and date (as Long).
     * @param userId ID of User to delete letter for
     * @param groupId ID of Group that originally imported the letter
     * @param date Long representation of letter date
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void delete(Long userId, Long groupId, Long date) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get a List of all a User's letters, retrieved from FHIR.
     * @param userId ID of User to retrieve letters for
     * @return List of letters in FhirDocumentReference format
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<FhirDocumentReference> getByUserId(Long userId) throws ResourceNotFoundException, FhirResourceException;

    @UserOnly
    FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException;
}
