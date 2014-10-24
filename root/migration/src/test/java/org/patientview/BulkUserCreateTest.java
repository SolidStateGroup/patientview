package org.patientview;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.patientview.migration.service.UserDataMigrationService;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 22/10/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BulkUserCreateTest {

    @Inject
    private UserDataMigrationService userDataMigrationService;

    /**
     * Order(1) Bulk create users give group and number of users
     */
    @Test
    @Transactional
    @Rollback(false)
    @Ignore("To be run manually")
    public void test01BulkUserCreate() {
        // takes group to add users to and number of users to create
        userDataMigrationService.bulkUserCreate("RENALB", 1L);
    }
}
