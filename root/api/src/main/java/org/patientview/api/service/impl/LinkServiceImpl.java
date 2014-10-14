package org.patientview.api.service.impl;

import org.patientview.api.service.LinkService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

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

        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        if (!isMemberOfGroup(group, getCurrentUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        link.setGroup(group);

        if (link.getLinkType() != null && link.getLinkType().getId() != null) {
            link.setLinkType(lookupRepository.findOne(link.getLinkType().getId()));
        }

        return linkRepository.save(link);
    }

    public Link addCodeLink(final Long codeId, final Link link)
            throws ResourceNotFoundException {

        Code code = codeRepository.findOne(codeId);

        if (code == null) {
            throw new ResourceNotFoundException("Code not found");
        }

        link.setCode(code);

        if (link.getLinkType() != null && link.getLinkType().getId() != null) {
            link.setLinkType(lookupRepository.findOne(link.getLinkType().getId()));
        }

        return linkRepository.save(link);
    }

    public Link get(final Long linkId) throws ResourceNotFoundException, ResourceForbiddenException {
        Link link = linkRepository.findOne(linkId);

        if (link == null) {
            throw new ResourceNotFoundException("Contact point does not exist");
        }

        if (!isMemberOfGroup(link.getGroup(), getCurrentUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return link;
    }

    public void delete(final Long linkId) throws ResourceNotFoundException, ResourceForbiddenException {
        Link link = linkRepository.findOne(linkId);

        if (link == null) {
            throw new ResourceNotFoundException("Link does not exist");
        }

        if (!isMemberOfGroup(link.getGroup(), getCurrentUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        linkRepository.delete(linkId);
    }

    public Link save(final Link link) throws ResourceNotFoundException, ResourceForbiddenException {

        Link entityLink = linkRepository.findOne(link.getId());

        if (entityLink == null) {
            throw new ResourceNotFoundException("Link does not exist");
        }

        if (link.getGroup() != null) {
            if (!isMemberOfGroup(entityLink.getGroup(), getCurrentUser())) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        entityLink.setLink(link.getLink());
        entityLink.setName(link.getName());
        entityLink.setDisplayOrder(link.getDisplayOrder());
        return linkRepository.save(entityLink);
    }
}
