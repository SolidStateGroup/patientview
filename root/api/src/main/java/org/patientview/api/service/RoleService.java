package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/10/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface RoleService {
    List<Role> getAllRoles();

    List<Role> getRolesByType(RoleType type);

    @UserOnly
    List<Role> getUserRoles(Long userId);

    Role findByRoleTypeAndName(RoleType roleType, RoleName roleName);
}
