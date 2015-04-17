package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.DocumentReferenceBuilder;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.repository.AlertRepository;
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
    private FhirResource fhirResource;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private AlertRepository alertRepository;

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    private String nhsno;

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
        LOG.info(nhsno + ": Starting DocumentReference (letter) Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int success = 0;
        boolean verboseLogging = false;

        DocumentReferenceBuilder documentReferenceBuilder = new DocumentReferenceBuilder(data, patientReference);

        // get alert if present
        List<org.patientview.persistence.model.Identifier> identifiers = identifierRepository.findByValue(this.nhsno);

        if (!CollectionUtils.isEmpty(identifiers)) {
            List<Alert> alerts
                    = alertRepository.findByUserAndAlertType(identifiers.get(0).getUser(), AlertTypes.LETTER);
            if (!CollectionUtils.isEmpty(alerts)) {
                documentReferenceBuilder.setAlert(alerts.get(0));
            }
        }

        List<DocumentReference> documentReferences = documentReferenceBuilder.build();

        // get currently existing DocumentReference by subject Id
        Map<String, Date> existingMap = getExistingDateBySubjectId(fhirLink);

        if (!CollectionUtils.isEmpty(documentReferences)) {
            for (DocumentReference newDocumentReference : documentReferences) {

                // delete any existing DocumentReference for this Subject that have same date
                List<UUID> existingUuids = getExistingByDate(newDocumentReference, existingMap);
                if (!existingUuids.isEmpty()) {
                    for (UUID existingUuid : existingUuids) {
                        // logging for testing only
                        if (verboseLogging) {
                            if (newDocumentReference.getCreated() != null) {
                                LOG.info(nhsno + ": Deleting DocumentReference with date "
                                        + newDocumentReference.getCreated().getValue().toString());
                            } else {
                                LOG.info(nhsno + ": Deleting DocumentReference");
                            }
                        }
                        fhirResource.deleteEntity(existingUuid, "documentreference");
                    }
                }

                // create new DocumentReference
                try {
                    // logging for testing only
                    if (verboseLogging) {
                        if (newDocumentReference.getCreated() != null) {
                            LOG.info(nhsno + ": Adding DocumentReference with date "
                                    + newDocumentReference.getCreated().getValue().toString());
                        } else {
                            LOG.info(nhsno + ": Adding DocumentReference");
                        }
                    }
                    fhirResource.createEntity(
                            newDocumentReference, ResourceType.DocumentReference.name(), "documentreference");
                    success++;
                } catch (FhirResourceException e) {
                    LOG.error(nhsno + ": Unable to create DocumentReference");
                }
            }
        }

        Alert builderAlert = documentReferenceBuilder.getAlert();
        if (builderAlert != null) {
            Alert entityAlert = alertRepository.findOne(builderAlert.getId());
            if (entityAlert != null) {
                entityAlert.setLatestValue(builderAlert.getLatestValue());
                entityAlert.setLatestDate(builderAlert.getLatestDate());
                entityAlert.setWebAlertViewed(builderAlert.isWebAlertViewed());
                entityAlert.setEmailAlertSent(builderAlert.isEmailAlertSent());
                entityAlert.setLastUpdate(new Date());
                alertRepository.save(entityAlert);
            }
        }

        LOG.info(nhsno + ": Finished DocumentReference (letter) Process");
        LOG.info(nhsno + ": Processed {} of {} letters", success, documentReferenceBuilder.getCount());
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


