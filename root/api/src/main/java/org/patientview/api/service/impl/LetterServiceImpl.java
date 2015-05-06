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
import org.patientview.api.service.LetterService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class LetterServiceImpl extends AbstractServiceImpl<LetterServiceImpl> implements LetterService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

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
                                        LOG.info("Error checking for binary data, " +
                                                "File size cannot be found, ignoring");
                                    }
                                }
                            } catch (NumberFormatException nfe) {
                                LOG.info("Error checking for binary data, " +
                                        "Media reference to binary data is not Long, ignoring");
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

        if (userHasFileData(user, fileDataId)) {
            return fileDataRepository.getOne(fileDataId);
        } else {
            throw new ResourceNotFoundException("File not found");
        }
    }

    private boolean userHasFileData(User user, Long fileDataId) throws FhirResourceException {
        // get all FhirLink and check FileData exists for this DocumentReference for this user
        StringBuilder inString = new StringBuilder("'");
        FhirLink[] fhirLinks = user.getFhirLinks().toArray(new FhirLink[user.getFhirLinks().size()]);

        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getActive()) {
                inString.append("");
                inString.append(fhirLink.getResourceId().toString());
                inString.append("','");
            }
        }

        if (inString.length() > 0) {
            inString.delete(inString.length() - 2, inString.length());

            // retrieve Media url if they exist for these subjects
            StringBuilder query = new StringBuilder();
            query.append("SELECT CONTENT -> 'content' ->> 'url' FROM media WHERE logical_id::TEXT IN ");
            query.append("(SELECT content ->> 'location' ");
            query.append("FROM documentreference ");
            query.append("WHERE content -> 'subject' ->> 'display' IN (");
            query.append(inString.toString());
            query.append("))");

            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(query.toString());

                while ((results.next())) {
                    Long foundFileDataId = results.getLong(1);
                    if (fileDataId.equals(foundFileDataId)) {
                        connection.close();
                        return true;
                    }
                }

                connection.close();
            } catch (SQLException e) {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e2) {
                    throw new FhirResourceException(e2);
                }

                throw new FhirResourceException(e);
            }
        }
        return false;
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
            fhirResource.deleteEntity(uuid, "documentreference");
        }
    }
}
