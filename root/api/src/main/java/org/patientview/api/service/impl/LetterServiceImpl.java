package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.service.AuditService;
import org.patientview.service.FileDataService;
import org.patientview.api.service.LetterService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class LetterServiceImpl extends AbstractServiceImpl<LetterServiceImpl> implements LetterService {

    @Inject
    private AuditService auditService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private FileDataService fileDataService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<FhirDocumentReference> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return getByUserId(userId, null, null);
    }

    @Override
    public List<FhirDocumentReference> getByUserId(final Long userId, String fromDate, String toDate)
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

                if (fromDate != null && toDate != null) {
                    query.append(" AND CONTENT ->> 'created' >= '" + fromDate + "'");
                    query.append(" AND CONTENT ->> 'created' <= '" + toDate + "'");
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
                                if (fileDataRepository.exists(Long.valueOf(media.getContent().getUrlSimple()))) {
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

    @Override
    public FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (fileDataService.userHasFileData(user, fileDataId, ResourceType.DocumentReference)) {
            return fileDataRepository.getOne(fileDataId);
        } else {
            throw new ResourceNotFoundException("File not found");
        }
    }

    @Override
    public void addLetter(
            org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException {

        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

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

        fhirResource.createEntity(documentReference, ResourceType.DocumentReference.name(), "documentreference");
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
        Map<UUID, DocumentReference> documentReferenceMap = new HashMap<>();
        Map<UUID, FhirLink> fhirLinkMap = new HashMap<>();

        // get all letters
        for (FhirLink fhirLink : fhirLinkRepository.findByUserAndGroup(entityUser, entityGroup)) {
            List<UUID> logicalIds
                    = fhirResource.getLogicalIdsBySubjectId("documentreference", fhirLink.getResourceId());

            for (UUID logicalId : logicalIds) {
                fhirLinkMap.put(logicalId, fhirLink);
            }

            documentReferenceUuids.addAll(logicalIds);
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
                documentReferenceMap.put(uuid, documentReference);
            }
        }

        for (UUID uuid : documentReferenceUuidsToDelete) {
            fhirResource.deleteEntity(uuid, "documentreference");

            FhirLink fhirLink = fhirLinkMap.get(uuid);
            Group group = null;

            if (fhirLink != null && fhirLink.getGroup() != null) {
                group = fhirLink.getGroup();
            }

            Audit audit = new Audit();
            audit.setAuditActions(AuditActions.PATIENT_LETTER_DELETE);
            audit.setUsername(entityUser.getUsername());
            audit.setActorId(getCurrentUser().getId());
            audit.setGroup(group);
            audit.setSourceObjectId(entityUser.getId());
            audit.setSourceObjectType(AuditObjectTypes.User);

            DocumentReference doc = documentReferenceMap.get(uuid);

            String type = null;
            if (doc.getType() != null) {
                type = doc.getType().getTextSimple();
            }

            String dateString;
            if (doc.getCreated() != null) {
                dateString = doc.getCreated().getValue().toHumanDisplay();
            } else {
                dateString = "unknown date";
            }

            audit.setInformation(type + " (" + dateString + ")");
            auditService.save(audit);
        }
    }
}
