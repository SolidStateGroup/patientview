package org.patientview.importer.service.impl;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.service.AuditService;
import org.patientview.importer.service.GroupRoleService;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Service
public class GroupRoleServiceImpl extends AbstractServiceImpl<GroupRoleServiceImpl> implements GroupRoleService {

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRepository groupRoleRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuditService auditService;

    @Override
    public void add(Long userId, Long groupId, RoleType roleType) throws ResourceNotFoundException {

    }
}
