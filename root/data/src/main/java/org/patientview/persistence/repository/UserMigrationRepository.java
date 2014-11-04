package org.patientview.persistence.repository;

import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserMigrationRepository extends CrudRepository<UserMigration, Long> {

    @Query("SELECT um FROM UserMigration um WHERE um.status = :status")
    List<UserMigration> findByStatus(@Param("status") MigrationStatus status);

    @Query("SELECT um.patientview1UserId FROM UserMigration um WHERE um.status = :status")
    List<Long> findPatientview1IdsByStatus(@Param("status") MigrationStatus status);

    @Query("SELECT um FROM UserMigration um WHERE um.status <> :status")
    List<UserMigration> findByNotStatus(@Param("status") MigrationStatus status);
}
