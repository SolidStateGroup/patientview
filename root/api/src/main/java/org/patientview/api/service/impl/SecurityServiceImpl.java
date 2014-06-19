package org.patientview.api.service.impl;

import org.patientview.api.service.SecurityService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Service to supplied the create roles and permission based on a user
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class SecurityServiceImpl implements SecurityService {

    @Inject
    private RoleRepository roleRepository;

    public List<Role> getUserRoles(Long userId) {

        return Util.iterableToList(roleRepository.getValidRolesByUser(userId));

    }

}
