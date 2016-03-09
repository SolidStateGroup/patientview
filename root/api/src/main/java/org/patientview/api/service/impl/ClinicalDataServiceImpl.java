package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ClinicalDataService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
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

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirClinicalData.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null) {
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        // get FHIR Organization logical id UUID, creating from Group if not present already, used by Encounter
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
            try {
                fhirClinicalData.getDiagnosis().setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                conditionService.add(fhirClinicalData.getDiagnosis(), fhirLink);
            } catch (FhirResourceException fre) {
                return new ServerResponse("error saving diagnosis");
            }
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
