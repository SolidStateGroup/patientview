package org.patientview;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.repository.UnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
public class UserDataMigrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataMigrationTest.class);

    @Inject
    private UnitDao unitDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private UserDataMigrationService userDataMigrationService;

    @Test
    public void testUserMigration() {

        userDataMigrationService.migrate();

    }

}
