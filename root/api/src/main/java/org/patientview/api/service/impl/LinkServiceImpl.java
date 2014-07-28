package org.patientview.api.service.impl;

import org.patientview.api.service.LinkService;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@Service
public class LinkServiceImpl implements LinkService {

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private CodeRepository codeRepository;

    // TODO: Remove link creation
    public Link create(final Link link) {

        if (link.getCode() != null) {
            link.setCode(codeRepository.findOne(link.getCode().getId()));
        }

        if (link.getLinkType().getId() != null) {
            link.setLinkType(lookupRepository.findOne(link.getLinkType().getId()));
        }

        return linkRepository.save(link);
    }

    public Link getLink(final Long linkId) {
        return linkRepository.findOne(linkId);
    }

    public void deleteLink(final Long linkId) {
        linkRepository.delete(linkId);
    }
}
