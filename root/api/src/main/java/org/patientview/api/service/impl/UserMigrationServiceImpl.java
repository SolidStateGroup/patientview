package org.patientview.api.service.impl;

import org.patientview.api.service.UserMigrationService;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.repository.UserMigrationRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014.
 */
@Service
public class UserMigrationServiceImpl extends AbstractServiceImpl<UserMigrationServiceImpl>
        implements UserMigrationService {

    @Inject
    private UserMigrationRepository userMigrationRepository;

    public UserMigration save(UserMigration userMigration) {
        return userMigrationRepository.save(userMigration);
    }

    @Override
    public List<UserMigration> getByStatus(MigrationStatus migrationStatus) {
        return userMigrationRepository.findByStatus(migrationStatus);
    }

    @Override
    public List<Long> getPatientview1IdsByStatus(MigrationStatus migrationStatus) {
        return userMigrationRepository.findPatientview1IdsByStatus(migrationStatus);
    }
}
