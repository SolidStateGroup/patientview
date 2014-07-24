package org.patientview.persistence.repository;

import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This get all the routes associated with the user. Routes can be associated by a User's Group, Feature or Role
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RouteRepository extends CrudRepository<Route, Long> {

    @Query("SELECT r " +
           "FROM   Route r " +
           "JOIN   r.routeLinks rl " +
           "JOIN   rl.feature f " +
           "JOIN   f.userFeatures uf " +
           "WHERE  uf.user = :user ")
    public Iterable<Route> findFeatureRoutesByUser(@Param("user") User user);

    @Query("SELECT r " +
           "FROM   Route r " +
           "JOIN   r.routeLinks rl " +
           "JOIN   rl.group g " +
           "JOIN   g.groupRoles gr " +
           "WHERE  gr.user = :user ")
    public Iterable<Route> findGroupRoutesByUser(@Param("user") User user);

    @Query("SELECT r " +
           "FROM   Route r " +
           "JOIN   r.routeLinks rl " +
           "JOIN   rl.role rol " +
           "JOIN   rol.groupRoles gr " +
           "WHERE  gr.user = :user ")
    public Iterable<Route> findRoleRoutesByUser(@Param("user") User user);
}
