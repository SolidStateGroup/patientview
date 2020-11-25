package org.patientview.service.impl;

import generated.Patientview;
import generated.Patientview.Patient.Diagnostics.Diagnostic;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.DiagnosticReportBuilder;
import org.patientview.builder.MediaBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DiagnosticService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
@Service
public class DiagnosticServiceImpl extends AbstractServiceImpl<DiagnosticService> implements DiagnosticService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    private String nhsno;

    /**
     * Creates all of the FHIR DiagnosticReport and Observation (result) records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data     patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        boolean verboseLogging = false;
        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        if (verboseLogging) {
            LOG.info(nhsno + ": Starting DiagnosticReport and associated Observation (result) Process");
        }

        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;

        // delete existing
        deleteBySubjectId(fhirLink.getResourceId());

        if (verboseLogging) {
            LOG.info(nhsno + ": Deleted existing");
        }

        if (data.getPatient().getDiagnostics() != null) {
            for (Diagnostic diagnostic : data.getPatient().getDiagnostics().getDiagnostic()) {

                if (StringUtils.isNotEmpty(diagnostic.getDiagnosticresult())) {
                    Date now = new Date();

                    if (verboseLogging) {
                        LOG.info(nhsno + ": Building diagnostic " + count);
                    }

                    // build result observation
                    Observation observation = new Observation();
                    observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
                    observation.setStatusSimple(Observation.ObservationStatus.registered);

                    CodeableConcept value = new CodeableConcept();
                    value.setTextSimple(CommonUtils.cleanSql(diagnostic.getDiagnosticresult()));
                    observation.setValue(value);
                    observation.setSubject(patientReference);

                    CodeableConcept name = new CodeableConcept();
                    name.setTextSimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());
                    name.addCoding().setDisplaySimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.getName());
                    observation.setName(name);

                    Identifier identifier = new Identifier();
                    identifier.setLabelSimple("resultcode");
                    identifier.setValueSimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());
                    observation.setIdentifier(identifier);

                    // Build diagnostic report
                    DiagnosticReportBuilder diagnosticReportBuilder = new DiagnosticReportBuilder(diagnostic);
                    DiagnosticReport diagnosticReport = diagnosticReportBuilder.build();

                    try {
                        if (verboseLogging) {
                            LOG.info(nhsno + ": Saving diagnostic " + count);
                        }
                        // create result observation in FHIR
                        FhirDatabaseEntity storedObservation
                            = fhirResource.createEntity(observation, ResourceType.Observation.name(), "observation");

                        // get observation (result) reference and add to diagnostic report
                        ResourceReference resultReference = diagnosticReport.addResult();
                        resultReference.setDisplaySimple(storedObservation.getLogicalId().toString());

                        // set patient reference
                        diagnosticReport.setSubject(patientReference);

                        // if binary file then build media, store and add reference to DiagnosticReport
                        if (diagnostic.getDiagnosticfilebody() != null) {
                            // set filename and type if not set in XML
                            if (StringUtils.isEmpty(diagnostic.getDiagnosticfilename())) {
                                diagnostic.setDiagnosticfilename(String.valueOf(now.getTime()));
                            }
                            if (StringUtils.isEmpty(diagnostic.getDiagnosticfiletype())) {
                                diagnostic.setDiagnosticfiletype("application/unknown");
                            }
                            MediaBuilder mediaBuilder = new MediaBuilder(diagnostic);
                            mediaBuilder.build();
                            Media media = mediaBuilder.getMedia();

                            // create binary file
                            FileData fileData = new FileData();
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
                            byte[] content = CommonUtils.base64ToByteArray(diagnostic.getDiagnosticfilebody());
                            fileData.setContent(content);
                            fileData.setSize(Long.valueOf(content.length));

                            // store binary data
                            fileData = fileDataRepository.save(fileData);

                            // set Media file data ID and size
                            media = mediaBuilder.setFileDataId(media, fileData.getId());
                            media = mediaBuilder.setFileSize(media, content.length);

                            // create Media and set DocumentReference location to newly created Media logicalId
                            try {
                                // create Media
                                FhirDatabaseEntity createdMedia
                                        = fhirResource.createEntity(media, ResourceType.Media.name(), "media");

                                // create ResourceReference to newly created Media and add to new
                                // DiagnosticReportImageComponent on DiagnosticReport as Link
                                DiagnosticReport.DiagnosticReportImageComponent imageComponent
                                        = diagnosticReport.addImage();

                                ResourceReference mediaReference = new ResourceReference();
                                mediaReference.setDisplaySimple(createdMedia.getLogicalId().toString());
                                imageComponent.setLink(mediaReference);
                            } catch (FhirResourceException e) {
                                LOG.error(nhsno + ": Unable to create Media");
                            }
                        }

                        // create diagnostic report in FHIR
                        fhirResource.createEntity(
                                diagnosticReport, ResourceType.DiagnosticReport.name(), "diagnosticreport");

                        success += 1;

                        if (verboseLogging) {
                            LOG.info(nhsno + ": Saved diagnostic " + count);
                        }

                    } catch (FhirResourceException e) {
                        LOG.error(nhsno + ": Unable to build Observation (result) or DiagnosticReport");
                    }

                    count += 1;

                    if (verboseLogging) {
                        LOG.info(nhsno + ": Finished creating DiagnosticReport " + count);
                    }
                }
            }
        }

        LOG.info(nhsno + ": Processed {} of {} diagnostics", success, count);
    }

    @Override
    public void add(FhirDiagnosticReport fhirDiagnosticReport, FhirLink fhirLink) throws FhirResourceException {
        Date now = new Date();

        if (fhirDiagnosticReport.getResult() == null) {
            throw new FhirResourceException("no result");
        }

        // build diagnostic result observation
        Observation observation = new Observation();
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);

        CodeableConcept value = new CodeableConcept();
        value.setTextSimple(fhirDiagnosticReport.getResult().getValue());
        observation.setValue(value);
        observation.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());
        name.addCoding().setDisplaySimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.getName());
        observation.setName(name);

        Identifier identifier = new Identifier();
        identifier.setLabelSimple("resultcode");
        identifier.setValueSimple(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());
        observation.setIdentifier(identifier);

        // build diagnostic report
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        if (fhirDiagnosticReport.getDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirDiagnosticReport.getDate());
            DateTime diagnosticDate = new DateTime();
            diagnosticDate.setValue(dateAndTime);
            diagnosticReport.setDiagnostic(diagnosticDate);
        }

        if (StringUtils.isNotEmpty(fhirDiagnosticReport.getName())) {
            CodeableConcept reportName = new CodeableConcept();
            reportName.setTextSimple(fhirDiagnosticReport.getName());
            diagnosticReport.setName(reportName);
        }

        if (StringUtils.isNotEmpty(fhirDiagnosticReport.getType())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(fhirDiagnosticReport.getType());
            diagnosticReport.setServiceCategory(type);
        }

        // set diagnostic report patient reference
        diagnosticReport.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        // create observation
        FhirDatabaseEntity entity
                = fhirResource.createEntity(observation, ResourceType.Observation.name(), "observation");

        if (entity == null) {
            throw new FhirResourceException("error adding observation");
        }

        UUID observationUuid = entity.getLogicalId();

        // add observation (result) reference to diagnostic report
        ResourceReference resultReference = diagnosticReport.addResult();
        resultReference.setDisplaySimple(observationUuid.toString());

        FileData fileData = null;

        // if binary file then build media, store and add reference to DiagnosticReport
        if (StringUtils.isNotEmpty(fhirDiagnosticReport.getFileBase64())) {
            // set filename and type if not set
            if (StringUtils.isEmpty(fhirDiagnosticReport.getFilename())) {
                fhirDiagnosticReport.setFilename(String.valueOf(now.getTime()));
            }
            if (StringUtils.isEmpty(fhirDiagnosticReport.getFiletype())) {
                fhirDiagnosticReport.setFiletype("application/unknown");
            }
            MediaBuilder mediaBuilder = new MediaBuilder(fhirDiagnosticReport);
            mediaBuilder.build();
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
            byte[] content = CommonUtils.base64ToByteArray(fhirDiagnosticReport.getFileBase64());
            fileData.setContent(content);
            fileData.setSize(Long.valueOf(content.length));

            // store binary data
            fileData = fileDataRepository.save(fileData);

            if (fileData == null) {
                throw new FhirResourceException("error adding file data");
            }

            // set Media file data ID and size
            media = mediaBuilder.setFileDataId(media, fileData.getId());
            media = mediaBuilder.setFileSize(media, content.length);

            // create Media and set DocumentReference location to newly created Media logicalId
            try {
                // create Media
                FhirDatabaseEntity createdMedia
                        = fhirResource.createEntity(media, ResourceType.Media.name(), "media");

                // create ResourceReference to newly created Media and add to new
                // DiagnosticReportImageComponent on DiagnosticReport as Link
                DiagnosticReport.DiagnosticReportImageComponent imageComponent
                        = diagnosticReport.addImage();

                ResourceReference mediaReference = new ResourceReference();
                mediaReference.setDisplaySimple(createdMedia.getLogicalId().toString());
                imageComponent.setLink(mediaReference);
            } catch (FhirResourceException e) {
                fileDataRepository.delete(fileData);
                throw new FhirResourceException("Unable to create Media, cleared binary data");
            }
        }

        // create diagnostic report
        try {
            fhirResource.createEntity(diagnosticReport, ResourceType.DiagnosticReport.name(), "diagnosticreport");
        } catch (FhirResourceException e) {
            if (fileData != null) {
                fileDataRepository.delete(fileData);
                throw new FhirResourceException("Unable to create DiagnosticReport, cleared binary data");
            }

            throw new FhirResourceException("Unable to create DiagnosticReport");
        }
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException {
        deleteBySubjectIdAndDateRange(subjectId, null, null);
    }

    @Override
    public int deleteBySubjectIdAndDateRange(UUID subjectId, Date fromDate, Date toDate) throws FhirResourceException {
        int deleteCount = 0;

        // split query to avoid DELETE FROM observation WHERE logical_id::TEXT conversion of uuid to text
        StringBuilder query = new StringBuilder();
        query.append("SELECT CONTENT #> '{result,0}' ->> 'display', CONTENT #> '{image,0}' -> 'link' ->> 'display'");
        query.append("FROM diagnosticreport WHERE CONTENT -> 'subject' ->> 'display' = '");
        query.append(subjectId.toString());
        query.append("' ");

        if (fromDate != null && toDate != null) {
            // date range
            query.append("AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) >= '");
            query.append(fromDate);
            query.append("' AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) <= '");
            query.append(toDate);
            query.append("'");

            // get count of medication to be deleted
            List<UUID> uuidToDelete = fhirResource.getUuidByQuery(
                    "SELECT logical_id FROM diagnosticreport " +
                            "WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "' " +
                            "AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) >= '" + fromDate + "' " +
                            "AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) <= '" + toDate + "'"
            );

            deleteCount = uuidToDelete.size();
        }

        StringBuilder observationIn = new StringBuilder("'");
        List<UUID> mediaUuids = new ArrayList<>();
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            while ((results.next())) {
                observationIn.append(results.getString(1));
                observationIn.append("','");
                if (results.getString(2) != null) {
                    mediaUuids.add(UUID.fromString(results.getString(2)));
                }
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    throw new FhirResourceException(e2);
                }
            }

            throw new FhirResourceException(e);
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        if (observationIn.length() > 2) {
            observationIn.delete(observationIn.length() - 2, observationIn.length());
            fhirResource.executeSQL("DELETE FROM observation WHERE logical_id IN (" + observationIn.toString() + ")");
        }

        // delete Media and binary file if present
        if (!mediaUuids.isEmpty()) {
            for (UUID mediaUuid : mediaUuids) {
                // delete associated media and binary data if present
                Media media = (Media) fhirResource.get(mediaUuid, ResourceType.Media);
                if (media != null) {
                    // delete media
                    fhirResource.deleteEntity(mediaUuid, "media");

                    // delete binary data
                    try {
                        if (fileDataRepository.existsById(Long.valueOf(media.getContent().getUrlSimple()))) {
                            fileDataRepository.deleteById(Long.valueOf(media.getContent().getUrlSimple()));
                        }
                    } catch (NumberFormatException nfe) {
                        LOG.info("Error deleting existing binary data, " +
                                "Media reference to binary data is not Long, ignoring");
                    }
                }
            }
        }

        // delete DiagnosticReport
        query = new StringBuilder();
        query.append("DELETE FROM diagnosticreport WHERE CONTENT -> 'subject' ->> 'display' = '");
        query.append(subjectId.toString());
        query.append("' ");

        if (fromDate != null && toDate != null) {
            // date range
            query.append("AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) >= '");
            query.append(fromDate);
            query.append("' AND CAST(content ->> 'diagnosticDateTime' AS TIMESTAMP) <= '");
            query.append(toDate);
            query.append("'");
        }

        fhirResource.executeSQL(query.toString());

        return deleteCount;
    }
}
