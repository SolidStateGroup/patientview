package org.patientview.persistence.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.config.TestPersistenceConfig;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class UserTokenRepositoryTest {

    @Inject
    private UserTokenRepository userTokenRepository;

    @Inject
    private UserRepository userRepository;

    /**
     * Test: First DAO test needs TODO refactoring into base test
     *
     */
    @Test
    @Ignore
    public void testCreateToken() {

        User user = new User();
        user.setId(1L);
        user.setCreated(new Date());
        user.setUsername("system");
        user.setStartDate(new Date());
        user.setFhirResourceId(UUID.randomUUID());
        user.setCreator(user);

        userRepository.save(user);

        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(user);
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userTokenRepository.save(userToken);
    }
}
