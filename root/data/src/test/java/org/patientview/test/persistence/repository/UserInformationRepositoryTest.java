package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.persistence.repository.UserInformationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class UserInformationRepositoryTest {

    @Inject
    private UserInformationRepository userInformationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testCreateInformation() {
        User user = dataTestUtils.createUser("testUser");
        UserInformation userInformation
                = dataTestUtils.createUserInformation(user, UserInformationTypes.SHOULD_KNOW, "You should know");

        UserInformation entity = userInformationRepository.findByUserAndType(user, UserInformationTypes.SHOULD_KNOW);

        Assert.assertTrue("Should find entry with correct value", entity.getValue().equals(userInformation.getValue()));
    }
}
