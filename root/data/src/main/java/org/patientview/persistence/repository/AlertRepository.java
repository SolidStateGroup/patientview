package org.patientview.persistence.repository;

import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
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
 * Created on 14/01/2015
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT   a " +
            "FROM    Alert a " +
            "JOIN    a.user u " +
            "WHERE   u = :user ")
    List<Alert> findByUser(@Param("user") User user);

    @Query("SELECT   a " +
            "FROM    Alert a " +
            "JOIN    a.user u " +
            "WHERE   u = :user " +
            "AND     a.alertType = :alertType")
    List<Alert> findByUserAndAlertType(@Param("user") User user, @Param("alertType") AlertTypes alertType);

    @Query("SELECT   a " +
            "FROM    Alert a " +
            "WHERE   a.user = :user " +
            "AND     a.observationHeading = :observationHeading")
    List<Alert> findByUserAndObservationHeading(@Param("user") User user,
                                                @Param("observationHeading") ObservationHeading observationHeading);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Alert WHERE user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Alert a WHERE a.emailAlert = true AND a.emailAlertSent = false")
    List<Alert> findByEmailAlertSetAndNotSent();

    @Query("SELECT a FROM Alert a WHERE a.mobileAlert = true AND a.mobileAlertSent = false")
    List<Alert> findByMobileAlertSetAndNotSent();
}
