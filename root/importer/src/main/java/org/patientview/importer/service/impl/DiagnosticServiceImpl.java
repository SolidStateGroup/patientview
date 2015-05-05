package org.patientview.importer.service.impl;

import generated.Patientview;
import generated.Patientview.Patient.Diagnostics.Diagnostic;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.utils.CommonUtils;
import org.patientview.importer.builder.DiagnosticReportBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.DiagnosticService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    @Named("fhir")
    private DataSource dataSource;

    private String nhsno;

    /**
     * Creates all of the FHIR DiagnosticReport and Observation (result) records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
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

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        // split query to avoid DELETE FROM observation WHERE logical_id::TEXT conversion of uuid to text
        StringBuilder query = new StringBuilder();
        query.append("SELECT CONTENT #> '{result,0}' ->> 'display' ");
        query.append("FROM diagnosticreport WHERE CONTENT -> 'subject' ->> 'display' = '");
        query.append(subjectId.toString());
        query.append("'");

        StringBuilder inStatement = new StringBuilder("'");
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            while ((results.next())) {
                inStatement.append(results.getString(1));
                inStatement.append("','");
            }

            connection.close();
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }

        if (inStatement.length() > 2) {
            inStatement.delete(inStatement.length() - 2, inStatement.length());
            fhirResource.executeSQL("DELETE FROM observation WHERE logical_id IN (" + inStatement.toString() + ")");
        }

        // delete DiagnosticReport
        fhirResource.executeSQL(
            "DELETE FROM diagnosticreport WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "'"
        );
    }
}


