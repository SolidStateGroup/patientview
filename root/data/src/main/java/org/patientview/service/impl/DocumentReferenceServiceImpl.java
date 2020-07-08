package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.DocumentReferenceBuilder;
import org.patientview.builder.MediaBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DocumentReferenceService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class DocumentReferenceServiceImpl extends AbstractServiceImpl<DocumentReferenceService>
        implements DocumentReferenceService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    /**
     * Creates all of the FHIR DocumentReference records from the Patientview object.
     * Links them to the Patient by subject.
     *
     * @param data     patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {
        Alert alert = null;
        boolean verboseLogging = false;
        String nhsno = data.getPatient().getPersonaldetails().getNhsno();
        if (verboseLogging) {
            LOG.info(nhsno + ": Starting DocumentReference (letter) Process");
        }
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;

        if (data.getPatient().getLetterdetails() != null) {

            Map<String, String> existingMap
                = fhirResource.getExistingLetterDocumentReferenceTypeAndContentBySubjectId(fhirLink.getResourceId());

            List<org.patientview.persistence.model.Identifier> identifiers
                    = identifierRepository.findByValue(nhsno);

            // get LETTER alert if exists
            if (!CollectionUtils.isEmpty(identifiers)) {
                List<Alert> alerts
                        = alertRepository.findByUserAndAlertType(identifiers.get(0).getUser(), AlertTypes.LETTER);
                if (!CollectionUtils.isEmpty(alerts)) {
                    alert = alerts.get(0);
                }
            }

            for (Patientview.Patient.Letterdetails.Letter letter : data.getPatient().getLetterdetails().getLetter()) {
                try {
                    // check content of letter is not null or empty (some xml comes without content)
                    if (StringUtils.isNotEmpty(letter.getLettercontent())) {
                        Date now = new Date();
                        // set up Media and DocumentReference builders and build DocumentReference
                        DocumentReferenceBuilder docBuilder = new DocumentReferenceBuilder(letter, patientReference);
                        MediaBuilder mediaBuilder = null;
                        DocumentReference documentReference = docBuilder.build();

                        // if binary file then build media
                        if (letter.getLetterfilebody() != null) {

                            // set filename and type if not set in XML
                            if (StringUtils.isEmpty(letter.getLetterfilename())) {
                                letter.setLetterfilename(String.valueOf(now.getTime()));
                            }
                            if (StringUtils.isEmpty(letter.getLetterfiletype())) {
                                letter.setLetterfiletype("application/unknown");
                            }
                            mediaBuilder = new MediaBuilder(letter);
                            mediaBuilder.build();

                            // set title of DocumentReference if possible (overwrites type)
                            if (StringUtils.isNotEmpty(letter.getLettertitle())) {
                                documentReference.setDescriptionSimple(letter.getLettertitle());
                            }
                        }

                        // delete existing document reference, media and binary data if present
                        List<UUID> existingUuids = getExistingByTypeAndContent(documentReference, existingMap);

                        if (!existingUuids.isEmpty()) {
                            for (UUID existingUuid : existingUuids) {
                                if (verboseLogging) {
                                    if (documentReference.getCreated() != null) {
                                        LOG.info(nhsno + ": Deleting DocumentReference with date "
                                                + documentReference.getCreated().getValue().toString());
                                    } else {
                                        LOG.info(nhsno + ": Deleting DocumentReference");
                                    }
                                }

                                String locationUuid
                                    = fhirResource.getLocationUuidFromLogicalUuid(existingUuid, "documentreference");

                                if (locationUuid != null) {
                                    // delete associated media and binary data if present
                                    Media media
                                        = (Media) fhirResource.get(UUID.fromString(locationUuid), ResourceType.Media);

                                    if (media != null) {
                                        // delete media
                                        fhirResource.deleteEntity(UUID.fromString(locationUuid), "media");

                                        // delete binary data
                                        try {
                                            if (fileDataRepository.existsById(Long.valueOf(
                                                    media.getContent().getUrlSimple()))) {
                                                fileDataRepository.deleteById(
                                                        Long.valueOf(media.getContent().getUrlSimple()));
                                            }
                                        } catch (NumberFormatException nfe) {
                                            LOG.info("Error deleting existing binary data, " +
                                                    "Media reference to binary data is not Long, ignoring");
                                        }
                                    }
                                }

                                fhirResource.deleteEntity(existingUuid, "documentreference");
                            }
                        }

                        boolean failed = false;
                        FileData fileData = null;

                        // create new binary file and Media if letter has file body (base64 binary)
                        if (letter.getLetterfilebody() != null) {
                            Media media = mediaBuilder.getMedia();

                            // create binary file
                            fileData = new FileData();
                            fileData.setCreated(now);
                            if (media.getContent().getTitle() != null) {
                                fileData.setName(media.getContent().getTitleSimple());
                            } else {
                                fileData.setName(String.valueOf(now.getTime()));
                            }
                            if (media.getContent().getContentType() != null) {
                                fileData.setType(media.getContent().getContentTypeSimple());
                            } else {
                                fileData.setType("application/unknown");
                            }
                            // convert base64 string to binary
                            byte[] content = CommonUtils.base64ToByteArray(letter.getLetterfilebody());
                            fileData.setContent(content);
                            fileData.setSize(Long.valueOf(content.length));
                            fileData = fileDataRepository.save(fileData);

                            media = mediaBuilder.setFileDataId(media, fileData.getId());
                            media = mediaBuilder.setFileSize(media, content.length);

                            // create Media and set DocumentReference location to newly created Media logicalId
                            try {
                                FhirDatabaseEntity createdMedia
                                        = fhirResource.createEntity(media, ResourceType.Media.name(), "media");
                                documentReference.setLocationSimple(createdMedia.getLogicalId().toString());
                            } catch (FhirResourceException e) {
                                LOG.error(nhsno + ": Unable to create Media");
                                failed = true;
                            }
                        }

                        // create new DocumentReference
                        if (!failed) {
                            try {
                                if (verboseLogging) {
                                    if (documentReference.getCreated() != null) {
                                        LOG.info(nhsno + ": Adding DocumentReference with date "
                                                + documentReference.getCreated().getValue().toString());
                                    } else {
                                        LOG.info(nhsno + ": Adding DocumentReference");
                                    }
                                }
                                fhirResource.createEntity(
                                        documentReference, ResourceType.DocumentReference.name(), "documentreference");
                            } catch (FhirResourceException e) {
                                LOG.error(nhsno + ": Unable to create DocumentReference");
                                failed = true;
                            }
                        }

                        // if any object creation failed, clean up binary data
                        if (failed) {
                            if (fileData != null) {
                                fileDataRepository.delete(fileData);
                                LOG.error(nhsno + ": Had to clean up binary data");
                            }
                        } else {
                            success++;
                            if (alert != null) {
                                if (alert.getLatestDate() == null) {
                                    alert.setLatestDate(CommonUtils.getDateFromString(letter.getLetterdate()));
                                    alert.setLatestValue(letter.getLettertype());
                                    alert.setEmailAlertSent(false);
                                    alert.setWebAlertViewed(false);
                                    alert.setUpdated(true);
                                } else {
                                    if (alert.getLatestDate().getTime()
                                            < CommonUtils.getDateFromString(letter.getLetterdate()).getTime()) {
                                        alert.setLatestDate(CommonUtils.getDateFromString(letter.getLetterdate()));
                                        alert.setLatestValue(letter.getLettertype());
                                        alert.setEmailAlertSent(false);
                                        alert.setWebAlertViewed(false);
                                        alert.setUpdated(true);
                                    }
                                }
                            }
                        }
                    }
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                } catch (Exception e) {
                    LOG.error("DocumentReference Exception: " + e.getMessage());
                    throw new FhirResourceException(e);
                }
                count++;
            }

            if (alert != null) {
                Optional<Alert> optionalAlert = alertRepository.findById(alert.getId());
                if (optionalAlert.isPresent()) {
                    Alert entityAlert = optionalAlert.get();
                    entityAlert.setLatestValue(alert.getLatestValue());
                    entityAlert.setLatestDate(alert.getLatestDate());
                    entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                    entityAlert.setEmailAlertSent(alert.isEmailAlertSent());
                    entityAlert.setLastUpdate(new Date());
                    alertRepository.save(entityAlert);
                }
            }
        }

        LOG.trace(nhsno + ": Finished DocumentReference (letter) Process");
        LOG.info(nhsno + ": Processed {} of {} letters", success, count);
    }

    @Override
    public void add(FhirDocumentReference fhirDocumentReference, FhirLink fhirLink) throws FhirResourceException {
        Alert alert = null;
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

        // get LETTER alert if exists for this user
        List<Alert> alerts
                = alertRepository.findByUserAndAlertType(fhirLink.getUser(), AlertTypes.LETTER);
        if (!CollectionUtils.isEmpty(alerts)) {
            alert = alerts.get(0);
        }

        // set up Media and DocumentReference builders and build DocumentReference
        DocumentReferenceBuilder docBuilder = new DocumentReferenceBuilder(fhirDocumentReference, patientReference);
        DocumentReference documentReference = docBuilder.build();
        Date now = new Date();
        FileData fileData = null;

        // delete existing, matched by type and content
        Map<String, String> existingMap
                = fhirResource.getExistingLetterDocumentReferenceTypeAndContentBySubjectId(fhirLink.getResourceId());
        List<UUID> existingUuids = getExistingByTypeAndContent(documentReference, existingMap);

        if (!existingUuids.isEmpty()) {
            for (UUID existingUuid : existingUuids) {
                String locationUuid = fhirResource.getLocationUuidFromLogicalUuid(existingUuid, "documentreference");

                if (locationUuid != null) {
                    // delete associated media and binary data if present
                    Media media = (Media) fhirResource.get(UUID.fromString(locationUuid), ResourceType.Media);

                    if (media != null) {
                        // delete media
                        fhirResource.deleteEntity(UUID.fromString(locationUuid), "media");

                        // delete binary data
                        try {
                            if (fileDataRepository.existsById(Long.valueOf(media.getContent().getUrlSimple()))) {
                                fileDataRepository.deleteById(Long.valueOf(media.getContent().getUrlSimple()));
                            }
                        } catch (NumberFormatException nfe) {
                            LOG.info("Error deleting existing letter binary data, " +
                                    "Media reference to binary data is not Long, ignoring");
                        }
                    }
                }

                fhirResource.deleteEntity(existingUuid, "documentreference");
            }
        }

        // create new binary file and Media if document reference has file body (base64 binary)
        if (fhirDocumentReference.getFileBase64() != null) {

            // set filename and type if not set in XML
            if (StringUtils.isEmpty(fhirDocumentReference.getFilename())) {
                fhirDocumentReference.setFilename(String.valueOf(now.getTime()));
            }
            if (StringUtils.isEmpty(fhirDocumentReference.getFiletype())) {
                fhirDocumentReference.setFiletype("application/unknown");
            }

            // build Media
            MediaBuilder mediaBuilder = new MediaBuilder(fhirDocumentReference);
            mediaBuilder.build();
            Media media = mediaBuilder.getMedia();

            // create binary file
            fileData = new FileData();
            fileData.setCreated(now);

            // title
            if (media.getContent().getTitle() != null) {
                fileData.setName(media.getContent().getTitleSimple());
            } else {
                fileData.setName(String.valueOf(now.getTime()));
            }

            // type
            if (media.getContent().getContentType() != null) {
                fileData.setType(media.getContent().getContentTypeSimple());
            } else {
                fileData.setType("application/unknown");
            }

            // convert base64 string to binary
            byte[] content = CommonUtils.base64ToByteArray(fhirDocumentReference.getFileBase64());
            fileData.setContent(content);
            fileData.setSize(Long.valueOf(content.length));

            // persist to patientview file data
            fileData = fileDataRepository.save(fileData);

            // update Media with extra fields
            media = mediaBuilder.setFileDataId(media, fileData.getId());
            media = mediaBuilder.setFileSize(media, content.length);

            // create Media and set DocumentReference location to newly created Media logicalId
            try {
                FhirDatabaseEntity createdMedia
                        = fhirResource.createEntity(media, ResourceType.Media.name(), "media");
                documentReference.setLocationSimple(createdMedia.getLogicalId().toString());
            } catch (FhirResourceException e) {
                fileDataRepository.delete(fileData);
                throw new FhirResourceException("Unable to create Media, cleared binary data");
            }
        }

        // create new DocumentReference
        try {
            fhirResource.createEntity(
                    documentReference, ResourceType.DocumentReference.name(), "documentreference");
        } catch (FhirResourceException e) {
            fileDataRepository.delete(fileData);
            throw new FhirResourceException("Unable to create DocumentReference, cleared binary data");
        }

        // handle updated alerts
        if (alert != null) {
            if (alert.getLatestDate() == null) {
                alert.setLatestDate(fhirDocumentReference.getDate());
                alert.setLatestValue(fhirDocumentReference.getType());
                alert.setEmailAlertSent(false);
                alert.setWebAlertViewed(false);
                alert.setUpdated(true);
            } else {
                if (fhirDocumentReference.getDate() != null
                        && alert.getLatestDate().getTime() < fhirDocumentReference.getDate().getTime()) {
                    alert.setLatestDate(fhirDocumentReference.getDate());
                    alert.setLatestValue(fhirDocumentReference.getType());
                    alert.setEmailAlertSent(false);
                    alert.setWebAlertViewed(false);
                    alert.setUpdated(true);
                }
            }

            Optional<Alert> optionalAlert = alertRepository.findById(alert.getId());
            if (optionalAlert.isPresent()) {
                Alert entityAlert = optionalAlert.get();
                entityAlert.setLatestValue(alert.getLatestValue());
                entityAlert.setLatestDate(alert.getLatestDate());
                entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                entityAlert.setEmailAlertSent(alert.isEmailAlertSent());
                entityAlert.setLastUpdate(new Date());
                alertRepository.save(entityAlert);
            }
        }
    }

    /**
     * Given a DocumentReference and a Map of key (existing DocumentReference UUIDs) to value (type + content) get
     * existing uuids by type and content
     *
     * @param documentReference DocumentReference to check for existing in FHIR
     * @param existingMap       Map<String, String> of key (DocumentReference UUID) to value (type + content)
     * @return List of logical UUID for existing DocumentReferences that match the input DocumentReference
     */
    private List<UUID> getExistingByTypeAndContent(DocumentReference documentReference,
                                                   Map<String, String> existingMap) {
        List<UUID> existingByDateAndContent = new ArrayList<>();

        if (documentReference.getDescriptionSimple() != null) {
            String type = "null";
            if (documentReference.getType() != null) {
                type = documentReference.getType().getTextSimple();
            }
            // replace used to fix migrated letters coming as duplicates
            String key = type + documentReference.getDescriptionSimple()
                    .replaceAll("\\s+", " ")
                    .replace("'", "''").replace("''''", "''")
                    .replace(" \n", "CARRIAGE_RETURN").replace("\n ", "CARRIAGE_RETURN")
                    .replace("CARRIAGE_RETURN", "\n");

            for (Map.Entry keyValue : existingMap.entrySet()) {
                if (keyValue.getValue() != null) {
                    String existing = (String) keyValue.getValue();
                    if (key.equals(existing)) {
                        existingByDateAndContent.add(UUID.fromString((String) keyValue.getKey()));
                    }
                }
            }
        }
        return existingByDateAndContent;
    }
}
