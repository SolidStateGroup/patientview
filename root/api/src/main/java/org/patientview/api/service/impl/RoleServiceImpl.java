package org.patientview.api.service.impl;

import org.patientview.api.service.RoleService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/10/2014.
 *
 */
@Service
public class RoleServiceImpl extends AbstractServiceImpl<RoleServiceImpl> implements RoleService {

    @Inject
    private RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return Util.convertIterable(roleRepository.findAll());
    }

    public List<Role> getRolesByType(RoleType type) {
        return Util.convertIterable(roleRepository.findByRoleType(type));
    }

    public List<Role> getUserRoles(Long userId) {
        return Util.convertIterable(roleRepository.findValidRolesByUser(userId));
    }

    @Override
    public Role findByRoleTypeAndName(RoleType roleType, RoleName roleName) {
        return roleRepository.findByRoleTypeAndName(roleType, roleName);
    }
}
