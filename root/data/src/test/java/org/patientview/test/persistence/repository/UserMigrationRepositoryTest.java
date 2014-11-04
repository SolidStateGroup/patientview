package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.repository.UserMigrationRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class UserMigrationRepositoryTest {

    @Inject
    private UserMigrationRepository userMigrationRepository;

    @Inject
    DataTestUtils dataTestUtils;

    @Test
    public void testSuccessful() {

        UserMigration userMigration = new UserMigration(1L, MigrationStatus.COMPLETED);
        userMigrationRepository.save(userMigration);
        UserMigration userMigration2 = new UserMigration(2L, MigrationStatus.PATIENT_FAILED);
        userMigrationRepository.save(userMigration2);

        List<UserMigration> migrationUsers = userMigrationRepository.findByStatus(MigrationStatus.COMPLETED);

        Assert.assertEquals("Should be 1 completed migration", 1, migrationUsers.size());
    }

    @Test
    public void testFailed() {

        UserMigration userMigration = new UserMigration(1L, MigrationStatus.COMPLETED);
        userMigrationRepository.save(userMigration);
        UserMigration userMigration2 = new UserMigration(2L, MigrationStatus.PATIENT_FAILED);
        userMigrationRepository.save(userMigration2);
        UserMigration userMigration3 = new UserMigration(3L, MigrationStatus.USER_FAILED);
        userMigrationRepository.save(userMigration3);

        List<UserMigration> migrationUsers = userMigrationRepository.findByNotStatus(MigrationStatus.COMPLETED);

        Assert.assertEquals("Should be 2 non completed migration", 2, migrationUsers.size());
    }
}
