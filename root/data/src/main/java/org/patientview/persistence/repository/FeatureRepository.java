package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
           "WHERE   u = :user ")
    public List<Feature> findByUser(@Param("user") User user);

    @Query("SELECT fea FROM Feature fea WHERE :featureType MEMBER OF fea.featureTypes")
    public Iterable<Feature> findByType(@Param("featureType") Lookup featureType);

    @Query("SELECT fea FROM Feature fea WHERE fea.name = :featureName")
    public Feature findByName(@Param("featureName") String featureName);
}
