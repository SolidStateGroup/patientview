package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface SecurityService {

    @UserOnly
    List<Role> getUserRoles(Long userId);

    /**
     * This method collates the routes for a user from three different paths. This has been split
     * into 3 seperate queries to avoid hibernate altering the query.
     *
     * User -> Group -> Routes
     * User -> Features -> Routes
     * User -> Roles -> Routes
     *
     * @param userId
     * @return
     */
    @UserOnly
    Set<Route> getUserRoutes(Long userId);

    List<Group> getGroupByUserAndRole(Long userId, Long roleId);

    /**
     * Get the groups that are assigned to the user.
     * N.B. SuperAdmin gets them all/
     *
     * @param userId
     * @return
     */
    @UserOnly
    List<Group> getUserGroups(Long userId);
}
