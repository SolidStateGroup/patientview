package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.DocumentService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Document service for managing documents stored in FHIR as DocumentReferences.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
@Service
@Transactional
public class DocumentServiceImpl extends AbstractServiceImpl<DocumentServiceImpl> implements DocumentService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<FhirDocumentReference> getByUserIdAndClass(final Long userId, final String fhirClass,
                                                           final String fromDate, final String toDate)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        List<FhirDocumentReference> fhirDocumentReferences = new ArrayList<>();
        List<FhirDocumentReference> fhirDocumentReferencesNoDate = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    documentreference ");
                query.append("WHERE   content -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                if (fhirClass != null) {
                    query.append("AND content -> 'class' ->> 'text' = '").append(fhirClass).append("' ");
                } else {
                    query.append("AND (content ->> 'class') IS NULL ");
                }

                if (fromDate != null && toDate != null) {
                    query.append(" AND CONTENT ->> 'created' >= '").append(fromDate).append("'");
                    query.append(" AND CONTENT ->> 'created' <= '").append(toDate).append("'");
                }

                query.append(" ORDER BY CONTENT ->> 'created' ");

                // get list of DocumentReference
                List<DocumentReference> documentReferences
                        = fhirResource.findResourceByQuery(query.toString(), DocumentReference.class);

                // for each, create new transport object
                for (DocumentReference documentReference : documentReferences) {
                    org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference
                            = new org.patientview.persistence.model.FhirDocumentReference(
                            documentReference, fhirLink.getGroup());

                    // if location is present on document reference means there is Media and binary data associated
                    if (documentReference.getLocation() != null) {
                        Media media = (Media) fhirResource.get(UUID.fromString(
                                documentReference.getLocationSimple()), ResourceType.Media);
                        if (media != null && media.getContent() != null && media.getContent().getUrl() != null) {
                            try {
                                if (fileDataRepository.existsById(Long.valueOf(media.getContent().getUrlSimple()))) {
                                    fhirDocumentReference.setFilename(media.getContent().getTitleSimple());
                                    fhirDocumentReference.setFiletype(media.getContent().getContentTypeSimple());
                                    fhirDocumentReference.setFileDataId(
                                            Long.valueOf(media.getContent().getUrlSimple()));
                                    try {
                                        fhirDocumentReference.setFilesize(
                                                Long.valueOf(media.getContent().getSizeSimple()));
                                    } catch (NumberFormatException nfe) {
                                        LOG.info("Error checking for binary data, "
                                                + "File size cannot be found, ignoring");
                                    }
                                }
                            } catch (NumberFormatException nfe) {
                                LOG.info("Error checking for binary data, "
                                        + "Media reference to binary data is not Long, ignoring");
                            }
                        }
                    }

                    if (fhirDocumentReference.getDate() != null) {
                        fhirDocumentReferences.add(new FhirDocumentReference(fhirDocumentReference));
                    } else {
                        fhirDocumentReferencesNoDate.add(new FhirDocumentReference(fhirDocumentReference));
                    }
                }
            }
        }

        // order by date descending
        Collections.sort(fhirDocumentReferences, new Comparator<FhirDocumentReference>() {
            public int compare(FhirDocumentReference fdr1, FhirDocumentReference fdr2) {
                return fdr2.getDate().compareTo(fdr1.getDate());
            }
        });

        fhirDocumentReferences.addAll(fhirDocumentReferencesNoDate);

        return fhirDocumentReferences;
    }
}
