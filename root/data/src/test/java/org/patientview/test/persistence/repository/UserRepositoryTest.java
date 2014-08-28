package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void findByGroupsRoles() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);
        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findByGroupsRoles("%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());

    }
}
