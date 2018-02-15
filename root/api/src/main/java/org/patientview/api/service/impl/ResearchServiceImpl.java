package org.patientview.api.service.impl;

import com.google.common.collect.Lists;
import org.patientview.api.service.ResearchService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class ResearchServiceImpl extends AbstractServiceImpl<ResearchServiceImpl> implements ResearchService {

    @Inject
    private EntityManager entityManager;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private ResearchStudyRepository researchStudyRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private StaticDataManager staticDataManager;

    @Inject
    private UserRepository userRepository;


    @Override
    public Long add(ResearchStudy researchStudy) {
        return null;
    }

    @Override
    public void delete(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException {

    }

    @Override
    public ResearchStudy get(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException {
        return null;
    }

    @Override
    public Page<ResearchStudy> getAll() throws ResourceNotFoundException, ResourceForbiddenException {

        List<ResearchStudy> list = Lists.newArrayList(researchStudyRepository.findAll());
        PageRequest pageable = createPageRequest(1, list.size(), null, null);

        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public Page<ResearchStudy> getAllForUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        return getAll();
    }

    @Override
    public void save(ResearchStudy researchItem) throws ResourceNotFoundException, ResourceForbiddenException {

    }
}
