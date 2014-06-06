package org.patientview;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.migration.service.AdminDataService;
import org.patientview.repository.UnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;


/**
 * Created by james@solidstategroup.com
 * Created on 04/06/2014
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
public class AdminDataMigrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDataMigrationTest.class);


    @Inject
    private UnitDao unitDao;

    @Inject
    private AdminDataService adminDataService;

    @Before
    public void setup() {

    }

    @Test
    @Transactional
    public void testStaticDataMigrationFeatures()  throws Exception {
        LOG.info("Starting migration");
        //adminDataService.migrate();
        Assert.assertNotNull("This group should not be null", adminDataService.getLookupByName("UNIT"));
        Assert.assertNotNull("This feature should not be null", adminDataService.getFeatureByName("SHARING_THOUGHTS"));
        Assert.assertNotNull("This feature should not be null", adminDataService.getRoleByName("PATIENT"));
    }


}
