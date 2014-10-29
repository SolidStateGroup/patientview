package org.patientview.api.service.impl;

import org.patientview.api.service.FhirLinkService;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
@Service
public class FhirLinkServiceImpl extends AbstractServiceImpl<FhirLinkServiceImpl> implements FhirLinkService {

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    public FhirLink get(final Long fhirLinkId) {
        return fhirLinkRepository.findOne(fhirLinkId);
    }

    public void delete(final Long fhirLinkId) {
        fhirLinkRepository.delete(fhirLinkId);
    }

    public FhirLink save(final FhirLink fhirLink) {
        return fhirLinkRepository.save(fhirLink);
    }

    public FhirLink add(FhirLink fhirLink) {
        LOG.info("Not implemented");
        return null;
    }

    public FhirLink findByVersionId(String versionId) {
        return fhirLinkRepository.findByVersionUuid(UUID.fromString(versionId));
    }
}
