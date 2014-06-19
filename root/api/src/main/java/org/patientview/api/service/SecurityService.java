package org.patientview.api.service;

import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public interface SecurityService {

    List<Role> getUserRoles(Long userId);

    List<Route> getUserRoutes(Long userId);

}
