package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.api.service.DiagnosticService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
@Service
public class DiagnosticServiceImpl extends BaseController<DiagnosticServiceImpl> implements DiagnosticService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<org.patientview.api.model.FhirDiagnosticReport> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.FhirDiagnosticReport> fhirDiagnosticReports = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    diagnosticreport ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\", \"reference\": \"uuid\"}'");

                // get list of diagnostic reports
                List<DiagnosticReport> diagnosticReports
                    = fhirResource.findResourceByQuery(query.toString(), DiagnosticReport.class);

                // for each, create new transport object with result (Observation) found from resource reference
                for (DiagnosticReport diagnosticReport : diagnosticReports) {

                    if (diagnosticReport.getResult().isEmpty()) {
                        throw new FhirResourceException("No result found for Diagnostic Report");
                    }

                    try {
                        JSONObject resultJson = fhirResource.getResource(
                            UUID.fromString(diagnosticReport.getResult().get(0).getDisplaySimple()),
                            ResourceType.Observation);

                        Observation observation = (Observation) DataUtils.getResource(resultJson);
                        FhirDiagnosticReport fhirDiagnosticReport =
                                new FhirDiagnosticReport(diagnosticReport, observation, fhirLink.getGroup());

                        fhirDiagnosticReports.add(
                                new org.patientview.api.model.FhirDiagnosticReport(fhirDiagnosticReport));

                    } catch (Exception e) {
                        throw new FhirResourceException(e.getMessage());
                    }
                }
            }
        }

        return fhirDiagnosticReports;
    }

    @Override
    public void addDiagnosticReport(FhirDiagnosticReport fhirDiagnosticReport, FhirLink fhirLink) throws FhirResourceException {

        // build diagnostic result observation
        Observation observation = new Observation();
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);

        CodeableConcept value = new CodeableConcept();
        value.setTextSimple(fhirDiagnosticReport.getResult().getValue());
        observation.setValue(value);
        observation.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());
        name.addCoding().setDisplaySimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.getName());
        observation.setName(name);

        Identifier identifier = new Identifier();
        identifier.setLabelSimple("resultcode");
        identifier.setValueSimple(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());
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
        diagnosticReport.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        // create observation
        UUID observationUuid = FhirResource.getLogicalId(fhirResource.create(observation));

        // add observation (result) reference to diagnostic report
        ResourceReference resultReference = diagnosticReport.addResult();
        resultReference.setDisplaySimple(observationUuid.toString());

        // create diagnostic report
        fhirResource.create(diagnosticReport);
    }
}
