package org.patientview.importer.service.impl;

import generated.Patientview;
import generated.Patientview.Patient.Diagnostics.Diagnostic;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.importer.builder.DiagnosticReportBuilder;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.DiagnosticService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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

    /**
     * Creates all of the FHIR DiagnosticReport and Observation (result) records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting DiagnosticReport and associated Observation (result) Process");

        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;

        // delete existing
        deleteBySubjectId(fhirLink.getResourceId());

        if (data.getPatient().getDiagnostics() != null) {
            for (Diagnostic diagnostic : data.getPatient().getDiagnostics().getDiagnostic()) {

                // build result observation
                Observation observation = new Observation();
                observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
                observation.setStatusSimple(Observation.ObservationStatus.registered);

                CodeableConcept value = new CodeableConcept();
                value.setTextSimple(diagnostic.getDiagnosticresult());
                observation.setValue(value);
                observation.setSubject(patientReference);

                CodeableConcept name = new CodeableConcept();
                name.setTextSimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());
                name.addCoding().setDisplaySimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.getName());
                observation.setName(name);

                Identifier identifier = new Identifier();
                identifier.setLabelSimple("resultcode");
                identifier.setValueSimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());
                observation.setIdentifier(identifier);

                DiagnosticReportBuilder diagnosticReportBuilder = new DiagnosticReportBuilder(diagnostic);
                DiagnosticReport diagnosticReport = diagnosticReportBuilder.build();

                try {
                    // create result observation in FHIR
                    JSONObject storedObservation = fhirResource.create(observation);

                    // get observation (result) reference and add to diagnostic report
                    ResourceReference resultReference = diagnosticReport.addResult();
                    resultReference.setDisplaySimple(Util.getResourceId(storedObservation).toString());

                    // set patient reference
                    diagnosticReport.setSubject(patientReference);

                    // create diagnostic report in FHIR
                    fhirResource.create(diagnosticReport);

                    success += 1;

                } catch (FhirResourceException e) {
                    LOG.error("Unable to build Observation (result) or DiagnosticReport");
                }

                LOG.trace("Finished creating DiagnosticReport " + count++);
            }
        }

        LOG.info("Processed {} of {} diagnostics", success, count);
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("diagnosticreport", subjectId)) {

            // delete observation (result) associated with diagnostic report
            DiagnosticReport diagnosticReport
                    = (DiagnosticReport) fhirResource.get(uuid, ResourceType.DiagnosticReport);
            fhirResource.delete(UUID.fromString(diagnosticReport.getResult().get(0).getDisplaySimple()),
                    ResourceType.Observation);

            // delete diagnostic report
            fhirResource.delete(uuid, ResourceType.DiagnosticReport);
        }
    }
}


