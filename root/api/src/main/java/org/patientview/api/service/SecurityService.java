package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.persistence.model.Route;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Security service, much reduced since development began.
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface SecurityService {

    /**
     * This method collates the routes for a user from three different paths. This has been split
     * into 3 seperate queries to avoid hibernate altering the query.
     *
     * User -> Group -> Routes
     * User -> Features -> Routes
     * User -> Roles -> Routes
     *
     * @param userId ID of User to retrieve Routes for
     * @return Set of Routes
     */
    @UserOnly
    Set<Route> getUserRoutes(Long userId);
}
