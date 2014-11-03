package org.patientview;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
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
public class MigrationIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationIntegrationTest.class);

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private UserDataMigrationService userDataMigrationService;

    @Before
    public void setup() throws Exception {

    }

    /**
     * Order(1) Migrates all the static data like groups, specialities
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test01StaticDataMigrationFeatures()  throws Exception {
        LOG.info("Starting migration, must have -Durl=\"http://localhost:8080/api\" or equivalent");
        adminDataMigrationService.migrate();
        Assert.assertNotNull("UNIT lookup type should not be null", adminDataMigrationService.getLookupByName("UNIT"));
        Assert.assertNotNull("Roles.PATIENT should not be null", adminDataMigrationService.getRoleByName(RoleName.PATIENT));
        Assert.assertNotNull("Roles.UNIT_ADMIN should not be null", adminDataMigrationService.getRoleByName(RoleName.UNIT_ADMIN));
        Assert.assertNotNull("Roles.STAFF_ADMIN should not be null", adminDataMigrationService.getRoleByName(RoleName.STAFF_ADMIN));
    }

    /**
     * Order(2) Migrates all the user records into the new schema, including patient data if available
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test02UserMigration() {
        userDataMigrationService.migrate();
    }

    /**
     * Order(3) Log out as migration user (clear token)
     */
    @Test
    @Transactional
    @Rollback(false)
    public void test03Logout() {
        try {
            JsonUtil.logout();
        } catch (Exception e) {
            Assert.fail("Could not logout: " + e.getMessage());
        }
    }

}
