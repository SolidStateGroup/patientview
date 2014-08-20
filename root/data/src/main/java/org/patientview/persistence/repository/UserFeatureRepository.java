package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserFeatureRepository extends JpaRepository<UserFeature, Long> {

    @Modifying
    @Query("DELETE FROM UserFeature uf WHERE uf.user = :user")
    public void deleteByUser(@Param("user") User user);

    @Query("SELECT   uf " +
            "FROM    UserFeature uf " +
            "JOIN    uf.user u " +
            "JOIN    uf.feature f " +
            "WHERE   u = :user " +
            "AND     f = :feature ")
    public UserFeature findByUserAndFeature(@Param("user") User user, @Param("feature") Feature feature);
}
