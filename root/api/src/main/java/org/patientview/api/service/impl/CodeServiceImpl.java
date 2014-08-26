package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.CodeService;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class CodeServiceImpl extends AbstractServiceImpl<CodeServiceImpl> implements CodeService {

    @Inject
    private CodeRepository codeRepository;
    @Inject
    private LinkRepository linkRepository;
    @Inject
    private UserRepository userRepository;

    private List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(strings)) {
            for (String string : strings) {
                longs.add(Long.parseLong(string));
            }
        }
        return longs;
    }

    public Page<Code> getAllCodes(Pageable pageable, String filterText, String[] codeTypes, String[] standardTypes) {

        List<Long> codeTypesList = convertStringArrayToLongs(codeTypes);
        List<Long> standardTypesList = convertStringArrayToLongs(standardTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.toUpperCase() + "%";
        }

        if (ArrayUtils.isNotEmpty(codeTypes) && ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByCodeAndStandardTypesFiltered(filterText, codeTypesList,
                    standardTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(codeTypes)) {
            return codeRepository.findAllByCodeTypesFiltered(filterText, codeTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByStandardTypesFiltered(filterText, standardTypesList, pageable);
        }

        return codeRepository.findAllFiltered(filterText, pageable);
    }

    public Code add(final Code code) {
        Code newCode;

        Set<Link> links;
        // get links and features, avoid persisting until code created successfully
        if (!CollectionUtils.isEmpty(code.getLinks())) {
            links = new HashSet<>(code.getLinks());
            code.getLinks().clear();
        } else {
            links = new HashSet<>();
        }

        // save basic details
        try {
            newCode = codeRepository.save(code);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("Code not created, duplicate: {}", dve.getCause());
            throw new EntityExistsException("Code already exists");
        }

        // save links
        for (Link link : links) {
            link.setCode(newCode);
            link = linkRepository.save(link);
            newCode.getLinks().add(link);
        }

        return newCode;
    }

    public Code get(final Long codeId) {
        return codeRepository.findOne(codeId);
    }

    public Code save(final Code code) {
        return codeRepository.save(code);
    }

    public Code cloneCode(final Long codeId) {
        // clone original
        Code entityCode = codeRepository.findOne(codeId);
        Code newCode = (Code)SerializationUtils.clone(entityCode);

        // set up links
        newCode.setLinks(new HashSet<Link>());
        for (Link link : entityCode.getLinks()) {
            Link newLink = new Link();
            newLink.setLink(link.getLink());
            newLink.setName(link.getName());
            newLink.setDisplayOrder(link.getDisplayOrder());
            newLink.setCode(newCode);
            newLink.setLinkType(link.getLinkType());
            newLink.setCreator(userRepository.findOne(1L));
            newCode.getLinks().add(newLink);
        }
        newCode.setId(null);
        return codeRepository.save(newCode);
    }

    public void delete(final Long codeId) {
        codeRepository.delete(codeId);
    }

    public Link addLink(final Long codeId, final Link link) {
        Code entityCode = codeRepository.findOne(codeId);
        link.setCode(entityCode);
        link.setCreator(userRepository.findOne(1L));
        Link persistedLink = linkRepository.save(link);
        return persistedLink;
    }
}
