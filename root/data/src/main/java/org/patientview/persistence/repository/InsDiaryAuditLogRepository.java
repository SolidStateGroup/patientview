package org.patientview.persistence.repository;

import org.patientview.persistence.model.InsDiaryAuditLog;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 */
@Repository
public interface InsDiaryAuditLogRepository extends CrudRepository<InsDiaryAuditLog, Long> {

    List<InsDiaryAuditLog> findAll();

    @Query("SELECT a FROM InsDiaryAuditLog a WHERE a.patientId = :patientId")
    List<InsDiaryAuditLog> findByPatientId(@Param("patientId") Long patientId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InsDiaryAuditLog WHERE patientId = :patientId")
    void deleteByPatientId(@Param("patientId") Long patientId);
}