package org.patientview.api.service;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.NewsItem;
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
    List<Route> getUserRoutes(Long userId);

    List<Group> getGroupByUserAndRole(Long userId, Long roleId);

    /**
     * This is the method the retrieves the news for a news. News can be linked by :-
     *
     * User -> Roles -> News
     * User -> Groups -> News
     *
     * @param userId
     * @return
     */
    List<NewsItem> getNewsByUser(Long userId);
}
