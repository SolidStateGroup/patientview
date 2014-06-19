package org.patientview.persistence.repository;

import org.patientview.persistence.model.Route;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RouteRepository extends CrudRepository<Route, Long> {

    /**
     * This get all the routes associated with the user. Routes can be associated by a User's Group, Feature or Role
     *
     * @param userId
     * @return
     */
    @Query("SELECT r FROM Route r JOIN r.feature.userFeatures uf WHERE uf.user.id = :userId ")
    public Iterable<Route> getFeatureRoutesByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Route r JOIN r.group.groupRoles ug WHERE ug.user.id = :userId ")
    public Iterable<Route> getGroupRoutesByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Route r JOIN r.role.groupRoles rg WHERE rg.user.id = :userId")
    public Iterable<Route> getRoleRoutesByUserId(@Param("userId") Long userId);
}
