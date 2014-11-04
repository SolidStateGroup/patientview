package org.patientview.api.service;

import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserMigrationService {
    UserMigration save(UserMigration userMigration);

    List<UserMigration> getByStatus(MigrationStatus migrationStatus);

    List<Long> getPatientview1IdsByStatus(MigrationStatus migrationStatus);
}
