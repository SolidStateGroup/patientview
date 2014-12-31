package org.patientview.persistence.repository;

import org.patientview.persistence.model.AlertObservationHeading;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/12/2014
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface AlertObservationHeadingRepository extends JpaRepository<AlertObservationHeading, Long> {

    @Query("SELECT   aoh " +
            "FROM    AlertObservationHeading aoh " +
            "JOIN    aoh.user u " +
            "WHERE   u = :user ")
    public List<AlertObservationHeading> findByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM AlertObservationHeading WHERE user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
