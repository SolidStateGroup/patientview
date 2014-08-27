package org.patientview.api.service;

import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.springframework.data.domain.Page;
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
    Set<Route> getUserRoutes(Long userId);

    List<Group> getGroupByUserAndRole(Long userId, Long roleId);

    /**
     * Get the groups that are assigned to the user.
     * N.B. SuperAdmin gets them all/
     *
     * @param userId
     * @return
     */
    Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters);

    // allowed relationship groups are those that can be added as parents or children to existing groups
    // GLOBAL_ADMIN can see all groups so allowedRelationshipGroups is identical to those returned from getGroupsForUser
    // SPECIALTY_ADMIN can only edit their specialty and add relationships
    // all other users cannot add parents/children
    Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId);
}
