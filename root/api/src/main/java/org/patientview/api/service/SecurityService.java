package org.patientview.api.service;

import org.patientview.persistence.model.Role;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public interface SecurityService {

    List<Role> getUserRoles(Long userId);

}
