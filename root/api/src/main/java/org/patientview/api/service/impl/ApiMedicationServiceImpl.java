package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.GpMedicationService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirMedicationStatementRange;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpMedicationGroupCodes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.FhirLinkService;
import org.patientview.service.MedicationService;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class ApiMedicationServiceImpl extends AbstractServiceImpl<ApiMedicationServiceImpl> implements ApiMedicationService {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private MedicationService medicationService;

    @Inject
    private UserService userService;

    @Inject
    private GpMedicationService gpMedicationService;

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return getByUserId(userId, null, null);
    }

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userService.get(userId);

        List<FhirMedicationStatement> fhirMedications = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {

            boolean retrieveMedication = false;

            if (fhirLink.getGroup().getCode().equals(GpMedicationGroupCodes.ECS.toString())) {
                GpMedicationStatus gpMedicationStatus = gpMedicationService.getGpMedicationStatus(userId);
                if (gpMedicationStatus.getOptInStatus()) {
                    retrieveMedication = true;
                }
            } else {
                retrieveMedication = true;
            }

            if (retrieveMedication) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    medicationstatement ");
                query.append("WHERE   content -> 'patient' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                if (fromDate != null && toDate != null) {
                    query.append("AND   content -> 'whenGiven' ->> 'start' >= '" + fromDate + "' ");
                    query.append("AND   content -> 'whenGiven' ->> 'end' <= '" + toDate + "' ");
                }
                query.append(" ORDER BY  content -> 'whenGiven' ->> 'start' DESC ");

                // get list of medication statements
                List<MedicationStatement> medicationStatements
                        = fhirResource.findResourceByQuery(query.toString(), MedicationStatement.class);

                // for each, create new transport object with medication found from resource reference
                for (MedicationStatement medicationStatement : medicationStatements) {

                    try {
                        JSONObject medicationJson = fhirResource.getResource(
                                UUID.fromString(medicationStatement.getMedication().getDisplaySimple()),
                                ResourceType.Medication);

                        Medication medication = (Medication) DataUtils.getResource(medicationJson);

                        org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement =
                                new org.patientview.persistence.model.FhirMedicationStatement(
                                        medicationStatement, medication, fhirLink.getGroup());

                        fhirMedications.add(new FhirMedicationStatement(fhirMedicationStatement));

                    } catch (Exception e) {
                        throw new FhirResourceException(e.getMessage());
                    }
                }
            }
        }

        return fhirMedications;
    }

    @Transactional
    @Override
    public ServerResponse importMedication(FhirMedicationStatementRange fhirMedicationStatementRange) {
        boolean deleteMedications = false;
        boolean insertMedications = false;

        if (StringUtils.isEmpty(fhirMedicationStatementRange.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirMedicationStatementRange.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }
        if (fhirMedicationStatementRange.getStartDate() == null && fhirMedicationStatementRange.getEndDate() != null) {
            return new ServerResponse("start date not set");
        }
        if (fhirMedicationStatementRange.getStartDate() != null && fhirMedicationStatementRange.getEndDate() == null) {
            return new ServerResponse("end date not set");
        }
        if (fhirMedicationStatementRange.getStartDate() != null) {
            deleteMedications = true;
        }
        if (!CollectionUtils.isEmpty(fhirMedicationStatementRange.getMedications())) {
            insertMedications = true;
        }
        if (!deleteMedications && !insertMedications) {
            return new ServerResponse("must enter either a date range or a list of medications to add");
        }

        Group group = groupRepository.findByCode(fhirMedicationStatementRange.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirMedicationStatementRange.getIdentifier());

        if (CollectionUtils.isEmpty(identifiers)) {
            return new ServerResponse("identifier not found");
        }
        if (identifiers.size() > 1) {
            return new ServerResponse("identifier not unique");
        }

        Identifier identifier = identifiers.get(0);
        User patientUser = identifier.getUser();

        if (patientUser == null) {
            return new ServerResponse("user not found");
        }

        // make sure importer and patient from the same group
        if (!userService.currentUserSameUnitGroup(patientUser, RoleName.IMPORTER)) {
            LOG.error("Importer trying to import medication for patient outside his group");
            return new ServerResponse("Forbidden");
        }

        // make sure patient is a member of the imported group
        if (!ApiUtil.userHasGroup(patientUser, group.getId())) {
            return new ServerResponse("patient not a member of imported group");
        }

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirMedicationStatementRange.getIdentifier(),
                patientUser.getFhirLinks());

        if (fhirLink == null && insertMedications) {
            try {
                fhirLink = fhirLinkService.createFhirLink(patientUser, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        StringBuilder info = new StringBuilder();

        // delete existing medication in date range
        if (fhirLink != null && deleteMedications) {
            try {
                int deletedCount = medicationService.deleteBySubjectIdAndDateRange(fhirLink.getResourceId(),
                        fhirMedicationStatementRange.getStartDate(), fhirMedicationStatementRange.getEndDate());
                info.append(", deleted ").append(deletedCount);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error deleting existing medication");
            }
        }

        // insert new medication
        if (insertMedications) {
            int success = 0;
            for (org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement
                    : fhirMedicationStatementRange.getMedications()) {
                try {
                    medicationService.add(fhirMedicationStatement, fhirLink);
                    success++;
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error inserting medication, " + success + " of "
                            + fhirMedicationStatementRange.getMedications().size() + " added");
                }
            }
            info.append(", added ").append(success);
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
