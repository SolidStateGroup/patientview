package org.patientview.persistence.repository;

import org.patientview.persistence.model.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AuditRepository extends CrudRepository<Audit, Long> {

    public List<Audit> findAll();

    @Query("SELECT a " +
            "FROM Audit a ")
    Page<Audit> findAll(Pageable pageable);

    @Query("SELECT a " +
            "FROM Audit a " +
            "WHERE a.sourceObjectId IN (" +
            "SELECT u.id FROM User u " +
            "JOIN u.groupRoles ugr " +
            "JOIN ugr.group g " +
            "WHERE g.id IN :groupIds)")
    Page<Audit> findAllByGroup(@Param("groupIds") List<Long> groupIds, Pageable pageable);
}