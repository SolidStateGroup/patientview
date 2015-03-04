package org.patientview.persistence.repository;

import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RequestRepository extends CrudRepository<Request, Long> {

    Page<Request> findAll(Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.group.id IN :groupIds")
    Page<Request> findAllByGroups(@Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.status IN :statuses")
    Page<Request> findAllByStatuses(@Param("statuses") List<RequestStatus> statuses, Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.status IN :statuses " +
            "AND    r.group.id IN :groupIds")
    Page<Request> findAllByStatusesAndGroups(@Param("statuses") List<RequestStatus> statuses,
                                                 @Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group.groupRoles gr " +
           "WHERE  gr.user = :user")
    Page<Request> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND r.group.id IN :groupIds")
    Page<Request> findByUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                          Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    r.status IN :statuses")
    Page<Request> findByUserAndStatuses(@Param("user") User user,
                                            @Param("statuses") List<RequestStatus> requestStatuses,
                                            Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    r.status IN :statuses " +
            "AND    r.group.id IN :groupIds")
    Page<Request> findByUserAndStatusesAndGroups(@Param("user") User user,
                                                    @Param("statuses") List<RequestStatus> statuses,
                                                    @Param("groupIds") List<Long> groupIds,
                                                    Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group rg " +
            "JOIN   rg.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<Request> findByParentUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.status IN :statuses " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<Request> findByParentUserAndStatuses(@Param("user") User user,
                                                    @Param("statuses") List<RequestStatus> statuses,
                                                    Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<Request> findByParentUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                                Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.status IN :statuses " +
           "AND    r.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT")
    Page<Request> findByParentUserAndStatusesAndGroups(@Param("user") User user,
                                                        @Param("statuses") List<RequestStatus> statuses,
                                                        @Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT  COUNT(1)  " +
            "FROM   Request r " +
            "WHERE    r.status = org.patientview.persistence.model.enums.RequestStatus.SUBMITTED")
    BigInteger countSubmitted();

    @Query("SELECT  COUNT(1)  " +
            "FROM   Request r " +
            "JOIN   r.group.groupRoles gr " +
            "WHERE  gr.user.id = :userId " +
            "AND    r.status = org.patientview.persistence.model.enums.RequestStatus.SUBMITTED")
    BigInteger countSubmittedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(1) " +
            "FROM   Request r " +
            "JOIN   r.group rg " +
            "JOIN   rg.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user.id = :userId " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND    r.status = org.patientview.persistence.model.enums.RequestStatus.SUBMITTED")
    BigInteger countSubmittedByParentUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Request r " +
            "WHERE r.forename = :forename " +
            "AND r.surname = :surname " +
            "AND r.dateOfBirth = :dateOfBirth ")
    void deleteByForenameSurnameDateOfBirth(@Param("forename") String forename, @Param("surname") String surname,
                                            @Param("dateOfBirth") Date dateOfBirth);
}
