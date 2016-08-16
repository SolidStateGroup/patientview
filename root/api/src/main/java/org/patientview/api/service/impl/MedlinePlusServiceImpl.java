package org.patientview.api.service.impl;

import org.patientview.api.client.MedlineplusApiClient;
import org.patientview.api.client.MedlineplusResponseJson;
import org.patientview.api.service.MedlinePlusService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeExternalStandard;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LinkTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MedlinePlusService implementation
 */
@Service
public class MedlinePlusServiceImpl extends AbstractServiceImpl<MedlinePlusServiceImpl> implements MedlinePlusService {

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Override
    @Transactional
    public void setLink(Code entityCode) {
        try {

            if (entityCode == null) {
                LOG.error("Missing Code, cannot add Medline Plus link");
                return;
            }

            Set<CodeExternalStandard> codeExternalStandards = new HashSet<>();
            if (!CollectionUtils.isEmpty(entityCode.getExternalStandards())) {
                codeExternalStandards = new HashSet<>(entityCode.getExternalStandards());
            }
            // for each code external standard add or update link
            for (CodeExternalStandard codeExternalStandard : codeExternalStandards) {
                codeExternalStandard.setCode(entityCode);

                setCodeExternalStandardLink(entityCode, codeExternalStandard);
            }
        } catch (Exception e) {
            LOG.error("Failed to add MediaPlus link to Code", e);
        }
    }

    @Override
    @Transactional
    public void setCodeExternalStandardLink(Code entityCode, CodeExternalStandard codeExternalEntity) {
        try {

            if (codeExternalEntity == null || entityCode == null) {
                LOG.error("Missing CodeExternalStandard or Code, cannot add Medline Plus link");
                return;
            }

            Date now = new Date();
            org.patientview.persistence.model.Link existingLink = null;

            // check Link exists already with Medline Plus description
            for (org.patientview.persistence.model.Link link : entityCode.getLinks()) {
                if (link.getLinkType() != null &&
                        LinkTypes.MEDLINE_PLUS.name().equals(link.getLinkType().getLookupType().getType().name())) {
                    existingLink = link;
                }
            }

            /**
             * Need to check what system to use to query the link ICD-10 or SNOMED-CT.
             * Will bring the same link url though, but still nice to have support
             *
             * Defaults to ICD-10
             */
            MedlineplusApiClient.CodeSystem codeSystem = MedlineplusApiClient.CodeSystem.ICD_10_CM;
            if (MedlineplusApiClient.CodeSystem.SNOMED_CT.nameCode().equals(
                    codeExternalEntity.getExternalStandard().getName())) {
                codeSystem = MedlineplusApiClient.CodeSystem.SNOMED_CT;
            }

            MedlineplusApiClient apiClient = MedlineplusApiClient
                    .newBuilder()
                    .setCodeSystem(codeSystem)
                    .build();
            MedlineplusResponseJson json = apiClient.getLink(codeExternalEntity.getCodeString());

            String linkUrl = null;

            // Deep down in json, need to check all the bits before getting url
            if (json.getFeed() != null &&
                    json.getFeed().getEntry() != null &&
                    json.getFeed().getEntry().length > 0 &&
                    json.getFeed().getEntry()[0].getLink().length > 0) {

                linkUrl = json.getFeed().getEntry()[0].getLink()[0].getHref();

                if (existingLink == null) {

                    Lookup linkType = lookupRepository.findByTypeAndValue(LookupTypes.LINK_TYPE,
                            LinkTypes.MEDLINE_PLUS.name());
                    // should have them already configured
                    if (linkType == null) {
                        throw new ResourceNotFoundException("Could not find MEDLINE_PLUS link type Lookup");
                    }
                    // no medline plus link exist create one Link
                    Link medlinePlusLink = new Link();

                    medlinePlusLink.setLinkType(linkType);
                    medlinePlusLink.setLink(linkUrl);
                    medlinePlusLink.setName(linkType.getDescription());
                    medlinePlusLink.setCode(entityCode);
                    medlinePlusLink.setCreator(getCurrentUser());
                    medlinePlusLink.setCreated(now);
                    medlinePlusLink.setLastUpdater(getCurrentUser());
                    medlinePlusLink.setLastUpdate(medlinePlusLink.getCreated());

                    if (entityCode.getLinks().isEmpty()) {
                        medlinePlusLink.setDisplayOrder(1);
                    } else {
                        medlinePlusLink.setDisplayOrder(entityCode.getLinks().size() + 1);
                    }

                    entityCode.getLinks().add(medlinePlusLink);
                    entityCode.setLastUpdater(getCurrentUser());
                } else {
                    // update existing MedlineLink link
                    existingLink.setLink(linkUrl);
                    existingLink.setLastUpdater(getCurrentUser());
                    existingLink.setLastUpdate(now);
                }
            } else {
                LOG.error("Could not find medline plus url for {}", codeExternalEntity.getCodeString());
            }

            entityCode.setLastUpdate(now);
            codeRepository.save(entityCode);

        } catch (Exception e) {
            LOG.error("Failed to add MediaPlus link to Code", e);
        }

    }
}
