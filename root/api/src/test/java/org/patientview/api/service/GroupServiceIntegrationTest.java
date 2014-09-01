package org.patientview.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class GroupServiceIntegrationTest {

    @Inject
    DataTestUtils dataTestUtils;

    @Test
    public void testDoNothing() {

    }
}
