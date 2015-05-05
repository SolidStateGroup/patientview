package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.DocumentReferenceBuilder;
import org.patientview.importer.builder.MediaBuilder;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.DocumentReferenceService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class DocumentReferenceServiceImpl extends AbstractServiceImpl<DocumentReferenceService> implements DocumentReferenceService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    private String nhsno;
    private Alert alert;

    /**
     * Creates all of the FHIR DocumentReference records from the Patientview object.
     * Links them to the Patient by subject.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        LOG.trace(nhsno + ": Starting DocumentReference (letter) Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;
        boolean verboseLogging = false;

        if (data.getPatient().getLetterdetails() != null) {

            // get currently existing DocumentReference by subject Id, map of <logical Id, applies>
            Map<String, Date> existingMap = getExistingDateBySubjectId(fhirLink);

            List<org.patientview.persistence.model.Identifier> identifiers
                    = identifierRepository.findByValue(this.nhsno);

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
                    // set up Media and DocumentReference builders and build DocumentReference
                    DocumentReferenceBuilder docBuilder = new DocumentReferenceBuilder(letter, patientReference);
                    MediaBuilder mediaBuilder = new MediaBuilder(letter);
                    DocumentReference documentReference = docBuilder.build();

                    // if binary file then build media
                    if (letter.getLetterfilebody() != null) {
                        mediaBuilder.build();

                        // set title of DocumentReference if possible (overwrites type)
                        if (StringUtils.isNotEmpty(letter.getLettertitle())) {
                            documentReference.setDescriptionSimple(letter.getLettertitle());
                        }
                    }

                    // delete existing document reference, media and binary data if present
                    List<UUID> existingUuids = getExistingByDate(documentReference, existingMap);
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

                            String locationUuid = getLocationUuidFromLogicalUuid(existingUuid);

                            if (locationUuid != null) {
                                // delete associated media and binary data if present
                                Media media
                                        = (Media) fhirResource.get(UUID.fromString(locationUuid), ResourceType.Media);
                                if (media != null) {
                                    // delete media
                                    fhirResource.deleteEntity(UUID.fromString(locationUuid), "media");

                                    // delete binary data
                                    try {
                                        fileDataRepository.delete(Long.valueOf(media.getContent().getUrlSimple()));
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
                        fileData.setCreated(new Date());
                        if (media.getContent().getTitle() != null) {
                            fileData.setName(media.getContent().getTitleSimple());
                        }
                        if (media.getContent().getContentType() != null) {
                            fileData.setType(media.getContent().getContentTypeSimple());
                        }
                        fileData = fileDataRepository.save(fileData);

                        media = mediaBuilder.setFileDataId(media, fileData.getId());

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
                                alert.setLatestDate(letter.getLetterdate().toGregorianCalendar().getTime());
                                alert.setLatestValue(letter.getLettertype());
                                alert.setEmailAlertSent(false);
                                alert.setWebAlertViewed(false);
                                alert.setUpdated(true);
                            } else {
                                if (alert.getLatestDate().getTime()
                                        < letter.getLetterdate().toGregorianCalendar().getTime().getTime()) {
                                    alert.setLatestDate(letter.getLetterdate().toGregorianCalendar().getTime());
                                    alert.setLatestValue(letter.getLettertype());
                                    alert.setEmailAlertSent(false);
                                    alert.setWebAlertViewed(false);
                                    alert.setUpdated(true);
                                }
                            }
                        }
                    }
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
                count++;
            }

            if (alert != null) {
                Alert entityAlert = alertRepository.findOne(alert.getId());
                if (entityAlert != null) {
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

    private String getLocationUuidFromLogicalUuid(UUID logicalId) throws FhirResourceException {
        String output = null;

        StringBuilder query = new StringBuilder();
        query.append("SELECT content->>'location' ");
        query.append("FROM documentreference ");
        query.append("WHERE logical_id = '");
        query.append(logicalId.toString());
        query.append("' ");

        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            while ((results.next())) {
                output = results.getString(1);
            }

            connection.close();
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }

        return output;
    }

    private Map<String, Date> getExistingDateBySubjectId(FhirLink fhirLink)
            throws FhirResourceException, SQLException {
        Map<String, Date> existingMap = new HashMap<>();

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content->>'created' ");
        query.append("FROM documentreference ");
        query.append("WHERE content -> 'subject' ->> 'display' = '");
        query.append(fhirLink.getResourceId());
        query.append("' ");

        // execute and return map of logical ids and applies
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            while ((results.next())) {
                try {
                    Date applies = null;

                    if (StringUtils.isNotEmpty(results.getString(2))) {
                        String dateString = results.getString(2);
                        XMLGregorianCalendar xmlDate
                                = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                        applies = xmlDate.toGregorianCalendar().getTime();
                    }

                    existingMap.put(results.getString(1), applies);
                } catch (DatatypeConfigurationException e) {
                    LOG.error(nhsno + ": Error getting existing DocumentReference", e);
                }
            }

            connection.close();
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }

        return existingMap;
    }

    private List<UUID> getExistingByDate(DocumentReference documentReference, Map<String, Date> existingMap) {
        List<UUID> existingByDate = new ArrayList<>();

        try {
            if (documentReference.getCreated() != null) {
                XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(
                        documentReference.getCreated().getValue().toString());
                Long applies = xmlDate.toGregorianCalendar().getTime().getTime();

                for (Map.Entry keyValue : existingMap.entrySet()) {
                    if (keyValue.getValue() != null) {
                        Date existing = (Date) keyValue.getValue();
                        if (applies == existing.getTime()) {
                            existingByDate.add(UUID.fromString((String) keyValue.getKey()));
                        }
                    }
                }
            }
        } catch (DatatypeConfigurationException e) {
            LOG.error(nhsno + ": Error converting DocumentReference created date");
            return existingByDate;
        }
        return existingByDate;
    }
}


