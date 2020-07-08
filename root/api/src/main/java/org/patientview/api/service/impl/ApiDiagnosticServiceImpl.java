package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.service.ApiDiagnosticService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDiagnosticReportRange;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.DiagnosticService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.FileDataService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private DiagnosticService diagnosticService;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private FileDataService fileDataService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<org.patientview.api.model.FhirDiagnosticReport> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

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
                                    if (fileDataRepository.existsById(Long.valueOf(media.getContent().getUrlSimple()))) {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        if (fileDataService.userHasFileData(user, fileDataId, ResourceType.DiagnosticReport)) {
            return fileDataRepository.getOne(fileDataId);
        } else {
            throw new ResourceNotFoundException("File not found");
        }
    }

    @Transactional
    @Override
    public ServerResponse importDiagnostics(FhirDiagnosticReportRange fhirDiagnosticReportRange) {

        boolean deleteDiagnostics = false;
        boolean insertDiagnostics = false;

        if (StringUtils.isEmpty(fhirDiagnosticReportRange.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirDiagnosticReportRange.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }
        if (fhirDiagnosticReportRange.getStartDate() == null && fhirDiagnosticReportRange.getEndDate() != null) {
            return new ServerResponse("start date not set");
        }
        if (fhirDiagnosticReportRange.getStartDate() != null && fhirDiagnosticReportRange.getEndDate() == null) {
            return new ServerResponse("end date not set");
        }
        if (fhirDiagnosticReportRange.getStartDate() != null) {
            deleteDiagnostics = true;
        }
        if (!CollectionUtils.isEmpty(fhirDiagnosticReportRange.getDiagnostics())) {
            insertDiagnostics = true;
        }
        if (!deleteDiagnostics && !insertDiagnostics) {
            return new ServerResponse("must enter either a date range or a list of medications to add");
        }

        // if inserting, validate diagnostics
        if (insertDiagnostics) {
            for (org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                    : fhirDiagnosticReportRange.getDiagnostics()) {
                if (fhirDiagnosticReport.getDate() == null) {
                    return new ServerResponse("diagnostic is missing date");
                }
                if (StringUtils.isEmpty(fhirDiagnosticReport.getName())) {
                    return new ServerResponse("diagnostic is missing name");
                }
                if (StringUtils.isEmpty(fhirDiagnosticReport.getType())) {
                    return new ServerResponse("diagnostic is missing type");
                }
                if (!Util.isInEnum(fhirDiagnosticReport.getType(), DiagnosticReportTypes.class)) {
                    return new ServerResponse("diagnostic type '" + fhirDiagnosticReport.getType()
                            + "' is an unsupported type");
                }
            }
        }

        Group group = groupRepository.findByCode(fhirDiagnosticReportRange.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirDiagnosticReportRange.getIdentifier());

        if (CollectionUtils.isEmpty(identifiers)) {
            return new ServerResponse("identifier not found");
        }
        if (identifiers.size() > 1) {
            return new ServerResponse("identifier not unique");
        }

        Identifier identifier = identifiers.get(0);
        User user = identifier.getUser();

        if (user == null) {
            return new ServerResponse("user not found");
        }

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirDiagnosticReportRange.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null && insertDiagnostics) {
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        StringBuilder info = new StringBuilder();

        // delete existing diagnostics in date range
        if (fhirLink != null && deleteDiagnostics) {
            try {
                int deletedCount = diagnosticService.deleteBySubjectIdAndDateRange(fhirLink.getResourceId(),
                        fhirDiagnosticReportRange.getStartDate(), fhirDiagnosticReportRange.getEndDate());
                info.append(", deleted ").append(deletedCount);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error deleting existing diagnostics");
            }
        }

        // insert new diagnostics
        if (insertDiagnostics) {
            int success = 0;
            for (org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                    : fhirDiagnosticReportRange.getDiagnostics()) {
                try {
                    diagnosticService.add(fhirDiagnosticReport, fhirLink);
                    success++;
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error inserting diagnostic, " + success + " of "
                            + fhirDiagnosticReportRange.getDiagnostics().size() + " added");
                }
            }
            info.append(", added ").append(success);
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
