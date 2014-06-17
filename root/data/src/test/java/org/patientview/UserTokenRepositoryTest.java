package org.patientview;

import org.junit.Ignore;
import org.junit.Test;
import org.patientview.persistence.config.PersistenceConfig;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@ContextConfiguration(classes = {PersistenceConfig.class})
@ComponentScan(basePackages = {"org.patientview.persistence.repository"})
@Transactional
public class UserTokenRepositoryTest {

    @Inject
    private UserTokenRepository userTokenRepository;

    @Inject
    private UserRepository userRepository;

    @Ignore
    @Test
    public void testCreateToken() {

        User user = userRepository.getOne(1L);


        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(user);
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userTokenRepository.save(userToken);

    }
}
