package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FeatureRepository extends CrudRepository<Feature, Long> {

    @Query("SELECT  f " +
           "FROM    User u " +
           "JOIN    u.userFeatures uf " +
           "JOIN    uf.feature f " +
           "WHERE   u.id = :userId " +
           "UNION   " +
           "SELECT  f " +
           "FROM   User u" +
           "JOIN   u.groupRoles.group.groupFeatures.feature f" +
           "WHERE  User.id = :userId ")
    public Iterable<Feature> getFeaturesByUser(@Param(value = "userId") Long userId);
}
