package org.patientview.importer.service.impl;

import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.service.AuditService;
import org.patientview.importer.service.GroupRoleService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Service
public class GroupRoleServiceImpl extends AbstractServiceImpl<GroupRoleServiceImpl> implements GroupRoleService {

    @Inject
    private AuditService auditService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, Long groupId, RoleType roleType) throws ResourceNotFoundException {
        /*User creator = getCurrentUser();
        User user = userRepository.getOne(userId);
        Group group = groupRepository.findOne(groupId);
        Role role = roleRepository.findByRoleType(roleType).get(0);

        if (group == null || role == null) {
            throw new ResourceNotFoundException("Group or Role not found");
        }

        if (groupRoleRepository.findByUserGroupRole(user, group, role) != null) {
            throw new EntityExistsException();
        }

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(user);
        groupRole.setGroup(group);
        groupRole.setRole(role);
        groupRole.setCreator(creator);
        groupRoleRepository.save(groupRole);

        auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, user.getUsername(),
                getCurrentUser(), userId, AuditObjectTypes.User, group);*/

    }
}
