package org.patientview.api.service.impl;

import org.patientview.api.service.LinkService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LinkTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@Service
public class LinkServiceImpl extends AbstractServiceImpl<LinkServiceImpl> implements LinkService {

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private GroupRepository groupRepository;

    public Link addGroupLink(final Long groupId, final Link link)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!isUserMemberOfGroup(getCurrentUser(), group)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        link.setGroup(group);

        if (link.getLinkType() != null && link.getLinkType().getId() != null) {
            Lookup foundLookup = lookupRepository.findById(link.getLinkType().getId()).orElse(null);
            link.setLinkType(foundLookup);
        }

        return linkRepository.save(link);
    }

    public Link addCodeLink(final Long codeId, final Link link)
            throws ResourceNotFoundException {

        Code code = codeRepository.findById(codeId)
                .orElseThrow(() -> new ResourceNotFoundException("Code not found"));

        link.setCode(code);

        if (link.getLinkType() != null && link.getLinkType().getId() != null) {
            Lookup foundLookup = lookupRepository.findById(link.getLinkType().getId()).orElse(null);
            link.setLinkType(foundLookup);
        } else {
            // defaults to custom Type when Link is added through UI
            Lookup foundLookup = lookupRepository.findById(LinkTypes.CUSTOM.id()).orElse(null);
            link.setLinkType(foundLookup);
        }

        Link savedLink = linkRepository.save(link);
        reorderLinks(code.getCode());
        return savedLink;

    }

    public Link get(final Long linkId) throws ResourceNotFoundException, ResourceForbiddenException {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link does not exist"));

        if (!isUserMemberOfGroup(getCurrentUser(), link.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return link;
    }

    public void delete(final Long linkId) throws ResourceNotFoundException, ResourceForbiddenException {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link does not exist"));


        if (!isUserMemberOfGroup(getCurrentUser(), link.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        linkRepository.deleteById(link.getId());
    }

    public Link save(final Link link) throws ResourceNotFoundException, ResourceForbiddenException {

        Link entityLink = linkRepository.findById(link.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Link does not exist"));

        if (entityLink == null) {
            throw new ResourceNotFoundException("Link does not exist");
        }

        if (link.getGroup() != null) {
            if (!isUserMemberOfGroup(getCurrentUser(), entityLink.getGroup())) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        entityLink.setLink(link.getLink());
        entityLink.setName(link.getName());
        entityLink.setDisplayOrder(link.getDisplayOrder());
        return linkRepository.save(entityLink);
    }

    public void reorderLinks(String code) {
        // update Code with link if does not exist
        Code entityCode = codeRepository.findOneByCode(code);
        if (entityCode == null) {
            LOG.error("Could not reorder links, Code for code {} no found", code);
            return;
        }

        Map<String, Link> linkMap = new HashMap<>();
        int linkDisplayOrder = 1;

        for (org.patientview.persistence.model.Link link : entityCode.getLinks()) {
            if (link.getLinkType() != null && LinkTypes.NHS_CHOICES.id() == link.getLinkType().getId()) {
                linkMap.put(LinkTypes.NHS_CHOICES.name(), link);
                linkDisplayOrder++;
            } else if (link.getLinkType() != null && LinkTypes.MEDLINE_PLUS.id() == link.getLinkType().getId()) {
                linkMap.put(LinkTypes.MEDLINE_PLUS.name(), link);
                linkDisplayOrder++;
            }
        }

        for (org.patientview.persistence.model.Link link : entityCode.getLinks()) {
            if (link.getLinkType() != null && LinkTypes.NHS_CHOICES.id() == link.getLinkType().getId()) {
                link.setDisplayOrder(1);
            } else if (link.getLinkType() != null && LinkTypes.MEDLINE_PLUS.id() == link.getLinkType().getId()) {
                if (linkMap.containsKey(LinkTypes.NHS_CHOICES.name())) {
                    link.setDisplayOrder(2);
                } else {
                    link.setDisplayOrder(1);
                }
            } else {
                link.setDisplayOrder(linkDisplayOrder++);
            }

            linkRepository.save(link);
        }
    }
}
