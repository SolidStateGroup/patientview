package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ClinicalDataService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
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
    private CodeRepository codeRepository;

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
    private LookupRepository lookupRepository;

    @Inject
    private OrganizationService organizationService;

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

        // validate child objects
        if (fhirClinicalData.getTreatment() != null && fhirClinicalData.getTreatment().getStatus() == null) {
            return new ServerResponse("treatment status must be set");
        }

        if (fhirClinicalData.getDiagnosis() != null && fhirClinicalData.getDiagnosis().getCode() == null) {
            return new ServerResponse("diagnosis code must be set");
        }

        if (fhirClinicalData.getDiagnosis() != null && fhirClinicalData.getDiagnosis().getDate() == null) {
            return new ServerResponse("diagnosis date must be set");
        }

        if (fhirClinicalData.getDiagnosis() != null
                && StringUtils.isNotEmpty(fhirClinicalData.getDiagnosis().getCode())) {
            Lookup diagnosisLookup = lookupRepository.findByTypeAndValue(
                    LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());

            if (diagnosisLookup == null) {
                return new ServerResponse("cannot get diagnosis lookup");
            }

            List<Code> codes = codeRepository.findAllByCodeAndType(
                    fhirClinicalData.getDiagnosis().getCode(), diagnosisLookup);

            if (CollectionUtils.isEmpty(codes)) {
                return new ServerResponse("diagnosis code is invalid");
            }
        }

        if (fhirClinicalData.getOtherDiagnoses() != null) {
            if (!CollectionUtils.isEmpty(fhirClinicalData.getOtherDiagnoses())) {
                for (FhirCondition diagnosis : fhirClinicalData.getOtherDiagnoses()) {
                    if (StringUtils.isEmpty(diagnosis.getCode())) {
                        return new ServerResponse("diagnoses codes must be set");
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
        if (fhirClinicalData.getTreatment() != null) {
            // erase existing TREATMENT Encounters, should only ever be one
            try {
                encounterService.deleteByUserAndType(user, EncounterTypes.TREATMENT);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error removing existing treatment");
            }

            // store new TREATMENT Encounter (only if set)
            if (StringUtils.isNotEmpty(fhirClinicalData.getTreatment().getStatus())) {
                try {
                    fhirClinicalData.getTreatment().setEncounterType(EncounterTypes.TREATMENT.toString());
                    encounterService.add(fhirClinicalData.getTreatment(), fhirLink, organizationUuid);
                    info.append(", saved treatment");
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error saving treatment");
                }
            } else {
                info.append(", removed treatment");
            }
        }

        // diagnosis (originally diagnosisedta in xml)
        if (fhirClinicalData.getDiagnosis() != null) {
            // erase existing DIAGNOSIS_EDTA type Condition, should only be one
            try {
                conditionService.deleteBySubjectIdAndType(fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS_EDTA);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error removing existing diagnosis");
            }

            // store new DIAGNOSIS_EDTA type Condition (if set)
            if (StringUtils.isNotEmpty(fhirClinicalData.getDiagnosis().getCode())) {
                try {
                    fhirClinicalData.getDiagnosis().setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                    conditionService.add(fhirClinicalData.getDiagnosis(), fhirLink);
                    info.append(", saved diagnosis");
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error saving diagnosis");
                }
            } else {
                info.append(", removed diagnosis");
            }
        }

        // other diagnoses (orginally diagnosis, list of diagnoses)
        if (fhirClinicalData.getOtherDiagnoses() != null) {
            // erase existing DIAGNOSIS type Condition, could be multiple
            try {
                conditionService.deleteBySubjectIdAndType(fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error removing existing diagnoses");
            }

            if (!CollectionUtils.isEmpty(fhirClinicalData.getOtherDiagnoses())) {
                // store new DIAGNOSIS type Conditions
                int successCount = 0;
                for (FhirCondition diagnosis : fhirClinicalData.getOtherDiagnoses()) {
                    try {
                        diagnosis.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                        conditionService.add(diagnosis, fhirLink);
                    } catch (FhirResourceException fre) {
                        return new ServerResponse("error saving diagnosis, added " + successCount
                                + " of " + fhirClinicalData.getOtherDiagnoses().size());
                    }
                }

                info.append(", saved ").append(fhirClinicalData.getOtherDiagnoses().size()).append(" other diagnoses");
            } else {
                info.append(", removed other diagnoses");
            }
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
