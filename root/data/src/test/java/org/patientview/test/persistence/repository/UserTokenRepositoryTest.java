package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.UserTokenTypes;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
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
    DataTestUtils dataTestUtils;

    @Test
    public void testCreateToken() {

        User user = dataTestUtils.createUser("testUser");

        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(user);
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userTokenRepository.save(userToken);
    }

    @Test
    public void testGetType_mobile() {
        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(dataTestUtils.createUser("testUser"));
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userToken.setType(UserTokenTypes.MOBILE);
        userTokenRepository.save(userToken);

        UserTokenTypes type = userTokenRepository.getType(userToken.getToken());

        Assert.assertEquals("should return correct type", UserTokenTypes.MOBILE, type);
    }

    @Test
    public void testGetType_web() {
        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(dataTestUtils.createUser("testUser"));
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userTokenRepository.save(userToken);

        // defaults to WEB
        UserTokenTypes type = userTokenRepository.getType(userToken.getToken());

        Assert.assertEquals("should return correct type", UserTokenTypes.WEB, type);
    }

    @Test
    public void testUpdateExpiration() {

        User user = dataTestUtils.createUser("testUser");

        // past = 30m in past, recent = 10m in past
        Date now = new Date();
        Date past = new Date(now.getTime() - 1800000);
        Date recent = new Date(now.getTime() - 600000);
        Date future = new Date(now.getTime() + 1800000);

        String token = "1234567890";
        UserToken userToken = new UserToken();
        userToken.setCreated(past);
        userToken.setUser(user);
        userToken.setToken(token);
        userToken.setExpiration(recent);
        userTokenRepository.save(userToken);

        userTokenRepository.setExpiration(token, future);
        UserToken updated = userTokenRepository.findByToken(token);

        Assert.assertTrue("Should update expiration", updated.getExpiration().after(recent));
    }

    @Test
    public void testGetExpiration() {

        User user = dataTestUtils.createUser("testUser");

        // past = 30m in past, recent = 10m in past
        Date now = new Date();
        Date past = new Date(now.getTime() - 1800000);
        Date recent = new Date(now.getTime() - 600000);
        Date future = new Date(now.getTime() + 1800000);

        String token = "1234567890";
        UserToken userToken = new UserToken();
        userToken.setCreated(past);
        userToken.setUser(user);
        userToken.setToken(token);
        userToken.setExpiration(recent);
        userTokenRepository.save(userToken);

        userTokenRepository.setExpiration(token, future);
        Date updatedExpiration = userTokenRepository.getExpiration(token);

        Assert.assertEquals("Should get correct expiration", future, updatedExpiration);
    }

    @Test
    public void testDeleteByUserId() {

        User user = dataTestUtils.createUser("testUser");
        UserToken userToken = new UserToken();
        userToken.setCreated(new Date());
        userToken.setUser(user);
        userToken.setToken("asdsa");
        userToken.setExpiration(new Date());
        userTokenRepository.save(userToken);

        userTokenRepository.deleteByUserId(user.getId());
        Assert.assertEquals("Should not get any UserToken", null, userTokenRepository.findByToken(userToken.getToken()));
    }
}
