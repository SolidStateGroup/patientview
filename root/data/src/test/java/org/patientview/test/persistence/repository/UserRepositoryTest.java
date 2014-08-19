package org.patientview.test.persistence.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/08/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testBlank() {

    }
}
