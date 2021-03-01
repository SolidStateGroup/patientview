package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ClinicalDataService;
import org.patientview.api.service.UserService;
import org.patientview.service.FhirLinkService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisSeverityTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.service.OrganizationService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Clinical data service, used by API importer to store treatment and diagnoses in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/03/2016
 */
@Service
public class ClinicalDataServiceImpl extends AbstractServiceImpl<ClinicalDataServiceImpl>
        implements ClinicalDataService {

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private UserService userService;

    @Transactional
    @Override
    public ServerResponse importClinicalData(FhirClinicalData fhirClinicalData) {
        if (StringUtils.isEmpty(fhirClinicalData.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirClinicalData.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }

        Group group = groupRepository.findByCode(fhirClinicalData.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirClinicalData.getIdentifier());

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

        // make sure importer and patient from the same group
        if (!userService.currentUserSameUnitGroup(user, RoleName.IMPORTER)) {
            LOG.error("Importer trying to import medication for patient outside his group");
            return new ServerResponse("Forbidden");
        }

        // make sure patient is a member of the imported group
        if (!ApiUtil.userHasGroup(user, group.getId())) {
            return new ServerResponse("patient not a member of imported group");
        }

        // validate treatments
        if (fhirClinicalData.getTreatments() != null) {
            if (!CollectionUtils.isEmpty(fhirClinicalData.getTreatments())) {
                for (FhirEncounter treatment : fhirClinicalData.getTreatments()) {
                    if (StringUtils.isEmpty(treatment.getStatus())) {
                        return new ServerResponse("treatment status must be set");
                    }
                }
            }
        }

        // validate diagnoses
        if (fhirClinicalData.getDiagnoses() != null) {
            if (!CollectionUtils.isEmpty(fhirClinicalData.getDiagnoses())) {
                for (FhirCondition diagnosis : fhirClinicalData.getDiagnoses()) {
                    if (StringUtils.isEmpty(diagnosis.getCode())) {
                        return new ServerResponse("diagnosis code must be set");
                    }
                    if (diagnosis.getDate() == null) {
                        return new ServerResponse("diagnosis date must be set");
                    }
                }
            }
        }

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirClinicalData.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null) {
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        // get FHIR Organization logical id UUID, creating from Group if not present, used by treatment Encounter
        UUID organizationUuid;

        try {
            organizationUuid = organizationService.add(group);
        } catch (FhirResourceException fre) {
            return new ServerResponse("error saving organization");
        }

        if (organizationUuid == null) {
            return new ServerResponse("error saving organization, is null");
        }

        StringBuilder info = new StringBuilder();

        // treatment
        if (fhirClinicalData.getTreatments() != null) {
            // erase existing TREATMENT Encounters
            try {
                encounterService.deleteBySubjectIdAndType(fhirLink.getResourceId(), EncounterTypes.TREATMENT);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error removing existing treatment");
            }

            // store new TREATMENT Encounter (only if set)
            if (!CollectionUtils.isEmpty(fhirClinicalData.getTreatments())) {
                int successCount = 0;
                for (FhirEncounter fhirEncounter : fhirClinicalData.getTreatments()) {
                    try {
                        fhirEncounter.setEncounterType(EncounterTypes.TREATMENT.toString());
                        encounterService.add(fhirEncounter, fhirLink, organizationUuid);
                        successCount++;
                    } catch (FhirResourceException fre) {
                        return new ServerResponse("error saving treatment, added " + successCount
                                + " of " + fhirClinicalData.getTreatments().size());
                    }
                }

                info.append(", saved ").append(fhirClinicalData.getTreatments().size()).append(" treatments");
            } else {
                info.append(", removed treatments");
            }
        }

        // diagnoses (originally diagnosis, list of diagnoses)
        if (fhirClinicalData.getDiagnoses() != null) {
            // erase existing DIAGNOSIS type Condition, could be multiple
            try {
                conditionService.deleteBySubjectIdAndType(fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error removing existing diagnoses");
            }

            if (!CollectionUtils.isEmpty(fhirClinicalData.getDiagnoses())) {
                // store new DIAGNOSIS type Conditions
                int successCount = 0;
                for (FhirCondition diagnosis : fhirClinicalData.getDiagnoses()) {
                    try {
                        diagnosis.setCategory(DiagnosisTypes.DIAGNOSIS.toString());

                        // set MAIN diagnosis as first in list
                        if (successCount == 0) {
                            diagnosis.setSeverity(DiagnosisSeverityTypes.MAIN.toString());
                        }

                        conditionService.add(diagnosis, fhirLink);
                        successCount++;
                    } catch (FhirResourceException fre) {
                        return new ServerResponse("error saving diagnosis, added " + successCount
                                + " of " + fhirClinicalData.getDiagnoses().size());
                    }
                }

                info.append(", saved ").append(fhirClinicalData.getDiagnoses().size()).append(" diagnoses");
            } else {
                info.append(", removed diagnoses");
            }
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
