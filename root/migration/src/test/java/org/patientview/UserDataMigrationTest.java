package org.patientview;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.migration.service.UserDataMigrationService;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
public class UserDataMigrationTest {

    @Inject
    private UserDataMigrationService userDataMigrationService;


    /**
     * Order(2) Migrates all the user records that are patients with groups into the new schema
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(false)
    public void testUserMigration() {
        userDataMigrationService.migrate();
    }

}
