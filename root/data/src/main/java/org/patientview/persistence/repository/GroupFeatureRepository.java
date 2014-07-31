package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
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
public interface GroupFeatureRepository extends CrudRepository<GroupFeature, Long> {
    @Query("SELECT  gf " +
            "FROM    GroupFeature gf " +
            "JOIN    gf.group g " +
            "JOIN    gf.feature f " +
            "WHERE   g = :group " +
            "AND     f = :feature ")
    public GroupFeature findByGroupAndFeature(@Param("group") Group user, @Param("feature") Feature feature);
}
