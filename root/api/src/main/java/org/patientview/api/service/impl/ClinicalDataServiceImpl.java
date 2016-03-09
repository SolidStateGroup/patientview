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
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

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

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirClinicalData.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null) {
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        StringBuilder info = new StringBuilder();

        return new ServerResponse(null, "done" + info.toString(), true);
    }
}
