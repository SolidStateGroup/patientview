package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.api.service.DiagnosticService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
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
    public List<FhirDiagnosticReport> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirDiagnosticReport> fhirDiagnosticReports = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    diagnosticreport ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getVersionId().toString());
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
                        fhirDiagnosticReports.add(new FhirDiagnosticReport(diagnosticReport, observation,
                                fhirLink.getGroup()));

                    } catch (Exception e) {
                        throw new FhirResourceException(e.getMessage());
                    }
                }
            }
        }

        return fhirDiagnosticReports;
    }
}
