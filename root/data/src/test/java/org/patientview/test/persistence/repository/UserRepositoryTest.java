package org.patientview.test.persistence.repository;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

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

    /**
     * Test: find number of locked users given a group id
     * Fail: Incorrect count returned
     */
    @Test
    public void testCountLockedUsersByGroup() {
        User user = dataTestUtils.createUser("testUser");
        user.setLocked(true);
        userRepository.save(user);
        User user2 = dataTestUtils.createUser("testUser2");
        user2.setLocked(false);
        userRepository.save(user2);
        Group group = dataTestUtils.createGroup("testGroup", creator);
        Role role = dataTestUtils.createRole("testRole", creator);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role, creator);
        GroupRole groupRole2 = dataTestUtils.createGroupRole(user2, group, role, creator);

        Assert.assertEquals("There should be a count of 1 locked user", (Long)1L,
                userRepository.countLockedUsersByGroup(group));
    }

    /**
     * Test: find number of inactive users (1 month no logins) given a group id
     * Fail: Incorrect count returned
     */
    @Test
    public void testCountInactiveUsersByGroup() {
        User user = dataTestUtils.createUser("testUser");
        user.setLastLogin(DateUtils.addDays(new Date(), -1));
        userRepository.save(user);
        User user2 = dataTestUtils.createUser("testUser2");
        user2.setLastLogin(DateUtils.addYears(new Date(), -1));
        userRepository.save(user2);
        User user3 = dataTestUtils.createUser("testUser3");
        user3.setLastLogin(null);
        userRepository.save(user3);
        Group group = dataTestUtils.createGroup("testGroup", creator);
        Role role = dataTestUtils.createRole("testRole", creator);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role, creator);
        GroupRole groupRole2 = dataTestUtils.createGroupRole(user2, group, role, creator);
        GroupRole groupRole3 = dataTestUtils.createGroupRole(user3, group, role, creator);

        Date now = new Date();
        Date pastDate = DateUtils.addMonths(now, -1);

        Assert.assertEquals("There should be a count of 1 inactive users", (Long)1L,
                userRepository.countInactiveUsersByGroup(group, pastDate, now));

        Assert.assertEquals("There should be a count of 1 users who never logged in", (Long)1L,
                userRepository.countNeverLoggedInUsersByGroup(group));
    }
}
