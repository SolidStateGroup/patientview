package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.LetterService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class LetterServiceImpl extends BaseController<LetterServiceImpl> implements LetterService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Override
    public List<FhirDocumentReference> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

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

                // get list of DocumentReference
                List<DocumentReference> documentReferences
                    = fhirResource.findResourceByQuery(query.toString(), DocumentReference.class);

                // for each, create new transport object
                for (DocumentReference documentReference : documentReferences) {
                    org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference
                            = new org.patientview.persistence.model.FhirDocumentReference(
                            documentReference, fhirLink.getGroup());

                    if (fhirDocumentReference.getDate() != null) {
                        fhirDocumentReferences.add(new FhirDocumentReference(fhirDocumentReference));
                    } else {
                        fhirDocumentReferencesNoDate.add(new FhirDocumentReference());
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

    @Override
    public void addLetter(
            org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException {

        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        if (StringUtils.isNotEmpty(fhirDocumentReference.getType())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(fhirDocumentReference.getType());
            documentReference.setType(type);
        }

        if (StringUtils.isNotEmpty(fhirDocumentReference.getContent())) {
            documentReference.setDescriptionSimple(fhirDocumentReference.getContent());
        }

        if (fhirDocumentReference.getDate() != null) {
            try {
                DateAndTime dateAndTime = new DateAndTime(fhirDocumentReference.getDate());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                documentReference.setCreated(date);
            } catch (NullPointerException npe) {
                throw new FhirResourceException("Letter timestamp is incorrectly formatted");
            }
        }

        fhirResource.create(documentReference);
    }

    @Override
    public void delete(Long userId, Long groupId, Long date) throws ResourceNotFoundException, FhirResourceException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("User not found");
        }
        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        List<UUID> documentReferenceUuids = new ArrayList<>();

        // get all letters
        for (FhirLink fhirLink : fhirLinkRepository.findByUserAndGroup(entityUser, entityGroup)) {
            documentReferenceUuids.addAll(
                    fhirResource.getLogicalIdsBySubjectId("documentreference", fhirLink.getResourceId()));
        }

        List<UUID> documentReferenceUuidsToDelete = new ArrayList<>();

        // get to be deleted by date
        for (UUID uuid : documentReferenceUuids) {
            DocumentReference documentReference
                    = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);

            DateAndTime dateAndTime = documentReference.getCreated().getValue();
            String dateString = dateAndTime.toString();
            DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
            org.joda.time.DateTime dateTime = parser2.parseDateTime(dateString);

            if (dateTime.getMillis() == date) {
                documentReferenceUuidsToDelete.add(uuid);
            }
        }

        for (UUID uuid : documentReferenceUuidsToDelete) {
            fhirResource.delete(uuid, ResourceType.DocumentReference);
        }
    }
}
