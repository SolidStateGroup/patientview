package org.patientview;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.persistence.model.enums.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class MigrationIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationIntegrationTest.class);

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private UserDataMigrationService userDataMigrationService;

    @Before
    public void setup() throws Exception {
        adminDataMigrationService.init();
    }

    /**
     * Order(1) Migrate all the static data like groups, specialities
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test01StaticDataMigrationFeatures() throws Exception {
        LOG.info("Starting migration, must have -Durl=\"http://localhost:8080/api\" or equivalent");
        adminDataMigrationService.migrate();
        Assert.assertNotNull("UNIT lookup type should not be null", adminDataMigrationService.getLookupByName("UNIT"));
        Assert.assertNotNull("Roles.PATIENT should not be null", adminDataMigrationService.getRoleByName(RoleName.PATIENT));
        Assert.assertNotNull("Roles.UNIT_ADMIN should not be null", adminDataMigrationService.getRoleByName(RoleName.UNIT_ADMIN));
        Assert.assertNotNull("Roles.STAFF_ADMIN should not be null", adminDataMigrationService.getRoleByName(RoleName.STAFF_ADMIN));
    }

    /**
     * Order(2) Migrate all the user records into the new schema, including patient data if available
     *
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test02UserMigration() throws JsonMigrationException {
        userDataMigrationService.migrate();
    }

    /**
     * Order(2) Migrate conversations
     *
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test03ConversationMigration() {
        // migrate conversation data mapping new users to old pv1 users
    }

    /**
     * Order(3) Migrate join requests
     *
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test04JoinRequestMigration() {
        // migrate join requests
    }

    /**
     * Order(4) Migrate news
     *
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test04NewsMigration() {
        // migrate news
    }

    /**
     * Order(99) Log out as migration user (clear token)
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test99Logout() {
        try {
            JsonUtil.logout();
        } catch (Exception e) {
            Assert.fail("Could not logout: " + e.getMessage());
        }
    }

}
