package org.patientview.api.service.impl;

import org.patientview.api.service.CodeService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class CodeServiceImpl implements CodeService {

    @Inject
    private CodeRepository codeRepository;
    @Inject
    private LinkRepository linkRepository;
    @Inject
    private UserRepository userRepository;

    public List<Code> getAllCodes() { return Util.iterableToList(codeRepository.findAll()); }

    public Code createCode(final Code code) {

        Code persistedCode = codeRepository.save(code);
        Set<Link> links = code.getLinks();

        if (!CollectionUtils.isEmpty(links)) {
            for (Link link : links) {
                link.setCode(persistedCode);
                link.setCreator(userRepository.findOne(1L));
                linkRepository.save(link);
            }
        }

        return persistedCode;
    }

    public Code getCode(final Long codeId) {
        return codeRepository.findOne(codeId);
    }

    public Code saveCode(final Code code) {
        return codeRepository.save(code);
    }

    public void deleteCode(final Long codeId) {
        codeRepository.delete(codeId);
    }
}
