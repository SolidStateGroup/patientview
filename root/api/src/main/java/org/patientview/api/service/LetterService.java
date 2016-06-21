package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

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

    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importLetter(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference);
}
