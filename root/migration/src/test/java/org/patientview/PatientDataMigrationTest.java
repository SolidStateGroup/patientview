package org.patientview;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.migration.service.PatientDataMigrationService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
public class PatientDataMigrationTest
{

    @Inject
    private PatientDataMigrationService patientDataMigrationService;


    /**
     * Order(3) Migrates all the patients into the database
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testPatientMigration() throws Exception {
        patientDataMigrationService.migrate();

    }



}
