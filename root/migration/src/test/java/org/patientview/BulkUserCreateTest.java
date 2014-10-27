package org.patientview;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.patientview.enums.Roles;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    protected final Logger LOG = LoggerFactory.getLogger(BulkUserCreateTest.class);

    /**
     * Order(1) Bulk create users give group and number of users
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test01BulkUserCreate() {

        Long numberOfUsersToCreate = 1L;
        LOG.info("Starting creation of " + numberOfUsersToCreate
                + " generated users, must have -Durl=\"http://localhost:8080/api\" or equivalent");

        Date start = new Date();
        Roles role = Roles.PATIENT;

        // takes group to add users to and number of users to create
        userDataMigrationService.bulkUserCreate("SGC04", numberOfUsersToCreate, role);

        LOG.info("Creation of " + numberOfUsersToCreate + " "  + role.toString() + " took "
                + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
    }

    /**
     * Order(2) Log out as migration user (clear token)
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test02Logout() {
        try {
            JsonUtil.logout();
        } catch (Exception e) {
            Assert.fail("Could not logout: " + e.getMessage());
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
