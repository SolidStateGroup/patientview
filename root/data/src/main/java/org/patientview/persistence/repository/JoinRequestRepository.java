package org.patientview.persistence.repository;

import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface JoinRequestRepository extends CrudRepository<JoinRequest, Long> {

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group.groupRoles gr " +
           "WHERE  gr.user = :user")
    Iterable<JoinRequest> findByUser(@Param("user") User user);

    @Query("SELECT jr " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    jr.status = :status")
    Iterable<JoinRequest> findByUserAndType(@Param("user") User user,
                                            @Param("status")JoinRequestStatus joinRequestStatus);
}
