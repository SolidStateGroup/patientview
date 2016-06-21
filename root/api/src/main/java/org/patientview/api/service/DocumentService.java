package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Document service for managing documents stored in FHIR as DocumentReferences.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
public interface DocumentService {

    /**
     * Get a List of all a User's documentreference documents, retrieved from FHIR.
     *
     * @param userId ID of User to retrieve documents for
     * @param fhirClass String of documentreference class to find
     * @param fromDate yyyy-mm-dd date to search from
     * @param toDate  yyyy-mm-dd date to search to
     * @return List of documents in FhirDocumentReference format
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<FhirDocumentReference> getByUserIdAndClass(Long userId, String fhirClass, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException;
}
