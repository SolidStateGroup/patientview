package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface JoinRequestRepository extends CrudRepository<JoinRequest, Long> {

    Iterable<JoinRequest> findByStatus(JoinRequestStatus status);

    @Query("SELECT  COUNT(1)  " +
            "FROM   JoinRequest jr " +
            "WHERE    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmitted();

    @Query("SELECT  COUNT(1)  " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmittedByUser(@Param("user") User user);

    @Query("SELECT COUNT(1) " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group jgr " +
            "JOIN   jgr.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmittedByParentUser(@Param("user") User user);


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
    Iterable<JoinRequest> findByUserAndStatus(@Param("user") User user,
                                              @Param("status") JoinRequestStatus joinRequestStatus);


    @Query("SELECT jr " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group jgr " +
            "JOIN   jgr.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Iterable<JoinRequest> findByParentUser(@Param("user") User user);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group jgr " +
           "JOIN   jgr.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    jr.status = :status " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Iterable<JoinRequest> findByParentUserAndStatus(@Param("user") User user,
                                                    @Param("status") JoinRequestStatus joinRequestStatus);
}
