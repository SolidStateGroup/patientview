package org.patientview.persistence.repository;

import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface JoinRequestRepository extends CrudRepository<JoinRequest, Long> {

    Page<JoinRequest> findAll(Pageable pageable);

    @Query("SELECT  jr " +
            "FROM   JoinRequest jr " +
            "WHERE  jr.group.id IN :groupIds")
    Page<JoinRequest> findAllByGroups(@Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT  jr " +
            "FROM   JoinRequest jr " +
            "WHERE  jr.status IN :statuses")
    Page<JoinRequest> findAllByStatuses(@Param("statuses") List<JoinRequestStatus> statuses, Pageable pageable);

    @Query("SELECT  jr " +
            "FROM   JoinRequest jr " +
            "WHERE  jr.status IN :statuses " +
            "AND    jr.group.id IN :groupIds")
    Page<JoinRequest> findAllByStatusesAndGroups(@Param("statuses") List<JoinRequestStatus> statuses,
                                                 @Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group.groupRoles gr " +
           "WHERE  gr.user = :user")
    Page<JoinRequest> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND jr.group.id IN :groupIds")
    Page<JoinRequest> findByUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                          Pageable pageable);

    @Query("SELECT jr " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    jr.status = :statuses")
    Page<JoinRequest> findByUserAndStatuses(@Param("user") User user,
                                            @Param("statuses") List<JoinRequestStatus> joinRequestStatuses,
                                            Pageable pageable);

    @Query("SELECT jr " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    jr.status IN :statuses " +
            "AND    jr.group.id IN :groupIds")
    Page<JoinRequest> findByUserAndStatusesAndGroups(@Param("user") User user,
                                                    @Param("statuses") List<JoinRequestStatus> statuses,
                                                    @Param("groupIds") List<Long> groupIds,
                                                    Pageable pageable);

    @Query("SELECT jr " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group jgr " +
            "JOIN   jgr.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<JoinRequest> findByParentUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group jgr " +
           "JOIN   jgr.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    jr.status IN :statuses " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<JoinRequest> findByParentUserAndStatuses(@Param("user") User user,
                                                    @Param("statuses") List<JoinRequestStatus> statuses,
                                                    Pageable pageable);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group jgr " +
           "JOIN   jgr.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    jr.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<JoinRequest> findByParentUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                                Pageable pageable);

    @Query("SELECT jr " +
           "FROM   JoinRequest jr " +
           "JOIN   jr.group jgr " +
           "JOIN   jgr.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    jr.status IN :statuses " +
           "AND    jr.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<JoinRequest> findByParentUserAndStatusesAndGroups(@Param("user") User user,
                                                        @Param("statuses") List<JoinRequestStatus> statuses,
                                                        @Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT  COUNT(1)  " +
            "FROM   JoinRequest jr " +
            "WHERE    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmitted();

    @Query("SELECT  COUNT(1)  " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group.groupRoles gr " +
            "WHERE  gr.user.id = :userId " +
            "AND    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmittedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(1) " +
            "FROM   JoinRequest jr " +
            "JOIN   jr.group jgr " +
            "JOIN   jgr.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user.id = :userId " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND    jr.status = org.patientview.persistence.model.enums.JoinRequestStatus.SUBMITTED")
    BigInteger countSubmittedByParentUser(@Param("userId") Long userId);
}
