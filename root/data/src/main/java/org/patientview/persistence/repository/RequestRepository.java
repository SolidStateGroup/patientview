package org.patientview.persistence.repository;

import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RequestTypes;
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
public interface RequestRepository extends CrudRepository<Request, Long> {

    @Query("SELECT r " +
            "FROM Request r " +
            "WHERE r.type IN :requestTypes")
    Page<Request> findAll(@Param("requestTypes") List<RequestTypes> requestTypes, Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.group.id IN :groupIds " +
            "AND r.type IN :requestTypes")
    Page<Request> findAllByGroups(@Param("groupIds") List<Long> groupIds,
                                  @Param("requestTypes") List<RequestTypes> requestTypes, Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.status IN :statuses " +
            "AND r.type IN :requestTypes")
    Page<Request> findAllByStatuses(@Param("statuses") List<RequestStatus> statuses,
                                    @Param("requestTypes") List<RequestTypes> requestTypes, Pageable pageable);

    @Query("SELECT  r " +
            "FROM   Request r " +
            "WHERE  r.status IN :statuses " +
            "AND    r.group.id IN :groupIds " +
            "AND r.type IN :requestTypes")
    Page<Request> findAllByStatusesAndGroups(@Param("statuses") List<RequestStatus> statuses,
                                                 @Param("groupIds") List<Long> groupIds,
                                                 @Param("requestTypes") List<RequestTypes> requestTypes,
                                                 Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group.groupRoles gr " +
           "WHERE  gr.user = :user " +
            "AND r.type IN :requestTypes")
    Page<Request> findByUser(@Param("user") User user, @Param("requestTypes") List<RequestTypes> requestTypes, 
                             Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND r.group.id IN :groupIds " +
           "AND r.type IN :requestTypes")
    Page<Request> findByUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                      @Param("requestTypes") List<RequestTypes> requestTypes, Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    r.status IN :statuses " +
            "AND r.type IN :requestTypes")
    Page<Request> findByUserAndStatuses(@Param("user") User user,
                                            @Param("statuses") List<RequestStatus> requestStatuses,
                                            @Param("requestTypes") List<RequestTypes> requestTypes, Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    r.status IN :statuses " +
            "AND    r.group.id IN :groupIds " +
            "AND r.type IN :requestTypes")
    Page<Request> findByUserAndStatusesAndGroups(@Param("user") User user,
                                                    @Param("statuses") List<RequestStatus> statuses,
                                                    @Param("groupIds") List<Long> groupIds,
                                                    @Param("requestTypes") List<RequestTypes> requestTypes,
                                                    Pageable pageable);

    @Query("SELECT r " +
            "FROM   Request r " +
            "JOIN   r.group rg " +
            "JOIN   rg.groupRelationships grs " +
            "JOIN   grs.objectGroup.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND r.type IN :requestTypes")
    Page<Request> findByParentUser(@Param("user") User user, @Param("requestTypes") List<RequestTypes> requestTypes,
                                   Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.status IN :statuses " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND r.type IN :requestTypes")
    Page<Request> findByParentUserAndStatuses(@Param("user") User user,
                                                    @Param("statuses") List<RequestStatus> statuses,
                                                    @Param("requestTypes") List<RequestTypes> requestTypes,
                                                    Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND r.type IN :requestTypes")
    Page<Request> findByParentUserAndGroups(@Param("user") User user, @Param("groupIds") List<Long> groupIds,
                                            @Param("requestTypes") List<RequestTypes> requestTypes,
                                            Pageable pageable);

    @Query("SELECT r " +
           "FROM   Request r " +
           "JOIN   r.group rg " +
           "JOIN   rg.groupRelationships grs " +
           "JOIN   grs.objectGroup.groupRoles gr " +
           "WHERE  gr.user = :user " +
           "AND    r.status IN :statuses " +
           "AND    r.group.id IN :groupIds " +
           "AND    grs.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.PARENT " +
            "AND r.type IN :requestTypes")
    Page<Request> findByParentUserAndStatusesAndGroups(@Param("user") User user,
                                                        @Param("statuses") List<RequestStatus> statuses,
                                                        @Param("groupIds") List<Long> groupIds,
                                                        @Param("requestTypes") List<RequestTypes> requestTypes,
                                                        Pageable pageable);

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
}
