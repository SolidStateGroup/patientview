package org.patientview.api.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.persistence.model.Group;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

    @Inject
    private GroupService groupService;

    /**
     * Test: Test for the persistence context on the GroupService findAll method
     *
     */
    @Test
    @Ignore
    public void testFindAll() {
        groupService.findAll();

    }

}
