package org.patientview.test.persistence.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

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
    public void testCreateToken() {

        User user = new User();
        user.setCreated(new Date());
        user.setUsername("system");
        user.setStartDate(new Date());
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
