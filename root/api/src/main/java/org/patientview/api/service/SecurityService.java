package org.patientview.api.service;

import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface SecurityService {

    List<Role> getUserRoles(Long userId);

    List<Route> getUserRoutes(Long userId);

}
