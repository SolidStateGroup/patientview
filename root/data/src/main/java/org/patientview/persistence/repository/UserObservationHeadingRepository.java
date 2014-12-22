package org.patientview.persistence.repository;

import org.patientview.persistence.model.UserObservationHeading;
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
@Transactional(propagation = Propagation.REQUIRED)
public interface UserObservationHeadingRepository extends JpaRepository<UserObservationHeading, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserObservationHeading WHERE user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
