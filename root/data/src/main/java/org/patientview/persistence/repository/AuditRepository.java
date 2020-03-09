package org.patientview.persistence.repository;

import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.enums.AuditActions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AuditRepository extends CrudRepository<Audit, Long> {

    @Modifying
    @Query("UPDATE Audit a SET a.actorId = NULL WHERE a.actorId IS NOT NULL AND a.actorId = :actorId")
    void removeActorId(@Param("actorId") Long actorId);

    public List<Audit> findAll();

    @Query("SELECT a " +
            "FROM Audit a " +
            "WHERE (a.creationDate >= :start AND a.creationDate <= :end)")
    Page<Audit> findAll(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +

            // filter
            "WHERE (((a.sourceObjectId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText)) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.User)) " +
            "OR (a.actorId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText))) " +
            "OR (a.sourceObjectId IN (SELECT u.id FROM User u JOIN u.identifiers i WHERE (i.identifier LIKE :filterText))) " +
            "OR (a.information LIKE :filterText) " +
            "OR (a.username LIKE :filterText)) " +
            "AND (a.creationDate >= :start AND a.creationDate <= :end) ")
    Page<Audit> findAllFiltered(@Param("start") Date start, @Param("end") Date end,
                                @Param("filterText") String filterText, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +
            "WHERE (((a.sourceObjectId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText)) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.User)) " +
            "OR (a.actorId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText))) " +
            "OR (a.sourceObjectId IN (SELECT u.id FROM User u JOIN u.identifiers i WHERE (i.identifier LIKE :filterText))) " +
            "OR (a.information LIKE :filterText) " +
            "OR (a.username LIKE :filterText)) " +
            "AND (a.creationDate >= :start AND a.creationDate <= :end) ")
    Page<Audit> findAllByIdentifierFiltered(@Param("start") Date start, @Param("end") Date end,
                                            @Param("filterText") String filterText, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +

            // groups
            "WHERE a.sourceObjectId IN (" +
            "SELECT u.id FROM User u " +
            "JOIN u.groupRoles ugr " +
            "JOIN ugr.group g " +
            "WHERE g.id IN (:groupIds)) " +
            "OR ((a.sourceObjectId IN (SELECT g.id FROM Group g WHERE (g.id IN (:groupIds)))) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.Group) " +
            "OR a.group.id IN (:groupIds)")
    Page<Audit> findAllBySourceGroup(@Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +

            // groups
            "WHERE ((a.sourceObjectId IN (" +
            "SELECT u.id FROM User u " +
            "JOIN u.groupRoles ugr " +
            "JOIN ugr.group g " +
            "WHERE g.id IN (:groupIds))) " +
            "OR ((a.sourceObjectId IN (SELECT g.id FROM Group g WHERE (g.id IN (:groupIds)))) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.Group) " +
            "OR a.group.id IN (:groupIds)) " +

            // filter
            "AND ((a.sourceObjectId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText)) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.User) " +
            "OR (a.actorId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText))) " +
            "OR (a.sourceObjectId IN (SELECT u.id FROM User u JOIN u.identifiers i WHERE (i.identifier LIKE :filterText))) " +
            "OR (a.information LIKE :filterText) " +
            "OR (a.username LIKE :filterText)) " +
            "AND (a.creationDate >= :start AND a.creationDate <= :end) ")
    Page<Audit> findAllBySourceGroupFiltered(@Param("start") Date start, @Param("end") Date end,
                                             @Param("filterText") String filterText,
                                             @Param("groupIds") List<Long> groupIds, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +
            "WHERE a.auditActions IN (:actions)")
    Page<Audit> findAllByAction(@Param("actions") List<AuditActions> actions, Pageable pageable);


    @Query("SELECT a " +
            "FROM Audit a " +

            // actions
            "WHERE a.auditActions IN :actions " +

            // filter
            "AND ((a.sourceObjectId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText)) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.User) " +
            "OR (a.actorId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText))) " +
            "OR (a.sourceObjectId IN (SELECT u.id FROM User u JOIN u.identifiers i WHERE (i.identifier LIKE :filterText))) " +
            "OR (a.information LIKE :filterText) " +
            "OR (a.username LIKE :filterText)) " +
            "AND (a.creationDate >= :start AND a.creationDate <= :end) ")
    Page<Audit> findAllByActionFiltered(@Param("start") Date start, @Param("end") Date end,
                                        @Param("filterText") String filterText,
                                        @Param("actions") List<AuditActions> actions, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +

            // groups
            "WHERE ((a.sourceObjectId IN (" +
            "SELECT u.id FROM User u " +
            "JOIN u.groupRoles ugr " +
            "JOIN ugr.group g " +
            "WHERE g.id IN (:groupIds))) " +
            "OR ((a.sourceObjectId IN (SELECT g.id FROM Group g WHERE (g.id IN (:groupIds)))) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.Group) " +
            "OR a.group.id IN (:groupIds)) " +

            // actions
            "AND a.auditActions IN (:actions)")
    Page<Audit> findAllByGroupAndAction(@Param("groupIds") List<Long> groupIds,
                                        @Param("actions") List<AuditActions> actions, Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +

            // groups
            "WHERE ((a.sourceObjectId IN (" +
            "SELECT u.id FROM User u " +
            "JOIN u.groupRoles ugr " +
            "JOIN ugr.group g " +
            "WHERE g.id IN (:groupIds))) " +
            "OR ((a.sourceObjectId IN (SELECT g.id FROM Group g WHERE (g.id IN (:groupIds)))) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.Group) " +
            "OR a.group.id IN (:groupIds)) " +

            // actions
            "AND a.auditActions IN (:actions) " +

            // filter
            "AND ((a.sourceObjectId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText)) " +
            "AND a.sourceObjectType = org.patientview.persistence.model.enums.AuditObjectTypes.User) " +
            "OR (a.actorId IN (SELECT u.id FROM User u WHERE (UPPER(u.username) LIKE :filterText))) " +
            "OR (a.sourceObjectId IN (SELECT u.id FROM User u JOIN u.identifiers i WHERE (i.identifier LIKE :filterText))) " +
            "OR (a.information LIKE :filterText) " +
            "OR (a.username LIKE :filterText)) " +
            "AND (a.creationDate >= :start AND a.creationDate <= :end) ")
    Page<Audit> findAllBySourceGroupAndActionFiltered(@Param("start") Date start, @Param("end") Date end,
                                                      @Param("filterText") String filterText,
                                                      @Param("groupIds") List<Long> groupIds,
                                                      @Param("actions") List<AuditActions> actions, Pageable pageable);

    @Query("SELECT a.group.id, count(a.group.id) " +
            "FROM Audit a " +
            "WHERE a.group.id IN (:groupIds) " +
            "AND a.auditActions IN (:actions) " +
            "AND (a.creationDate >= :since) " +
            "GROUP BY a.group.id " +
            "ORDER BY a.group.id " +
            "")
    List<Object[]> findAllByCountGroupAction(@Param("groupIds") List<Long> groupIds,
                                       @Param("since") Date since,
                                       @Param("actions") List<AuditActions> actions);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Audit a SET a.xml = NULL WHERE a.xml IS NOT NULL AND a.creationDate <= :date")
    void removeOldAuditXml(@Param("date") Date date);
}