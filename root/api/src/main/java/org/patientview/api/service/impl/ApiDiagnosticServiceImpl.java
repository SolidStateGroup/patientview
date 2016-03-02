package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.service.FileDataService;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.api.service.ApiDiagnosticService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FileDataRepository;
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
public class ApiDiagnosticServiceImpl extends AbstractServiceImpl<ApiDiagnosticServiceImpl>
        implements ApiDiagnosticService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private FileDataService fileDataService;

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

                        // if image array is present means there is Media and binary data associated (should only be 1)
                        if (!diagnosticReport.getImage().isEmpty()) {
                            Media media = (Media) fhirResource.get(UUID.fromString(
                                diagnosticReport.getImage().get(0).getLink().getDisplaySimple()), ResourceType.Media);
                            if (media != null && media.getContent() != null && media.getContent().getUrl() != null) {
                                try {
                                    if (fileDataRepository.exists(Long.valueOf(media.getContent().getUrlSimple()))) {
                                        fhirDiagnosticReport.setFilename(media.getContent().getTitleSimple());
                                        fhirDiagnosticReport.setFiletype(media.getContent().getContentTypeSimple());
                                        fhirDiagnosticReport.setFileDataId(
                                                Long.valueOf(media.getContent().getUrlSimple()));
                                        try {
                                            fhirDiagnosticReport.setFilesize(
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
    public FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (fileDataService.userHasFileData(user, fileDataId, ResourceType.DiagnosticReport)) {
            return fileDataRepository.getOne(fileDataId);
        } else {
            throw new ResourceNotFoundException("File not found");
        }
    }
}
