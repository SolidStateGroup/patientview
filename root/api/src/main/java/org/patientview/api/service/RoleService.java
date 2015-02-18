package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Role service, for management of User Roles.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/10/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface RoleService {

    /**
     * Find a Role given RoleType type and RoleName name.
     * @param roleType Type of Role
     * @param roleName Name of Role
     * @return Role object
     */
    Role findByRoleTypeAndName(RoleType roleType, RoleName roleName);

    /**
     * Get a List of all Roles.
     * @return List of Role objects
     */
    List<Role> getAllRoles();

    /**
     * Get a List of Roles by specifying type of Role to retrieve (staff or patient etc).
     * @param type Type of Role to retrieve
     * @return List of Role objects
     */
    List<Role> getRolesByType(RoleType type);

    /**
     * Get a List of available Roles for a User.
     * @param userId ID of User to retrieve available Roles
     * @return List of Role objects
     */
    @UserOnly
    List<Role> getUserRoles(Long userId);
}
