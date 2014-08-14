package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class AdminServiceIntegrationTest {

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FeatureRepository featureRepository;

    private User systemUser;

    private Role patientRole;

    private Feature messagingFeature;

    @Before
    public void setup() {

        systemUser = createSystemUser();
        patientRole = createRole("PATIENT");
        messagingFeature = createFeature("FEATURE");

    }


    /**
     * Test: Integration test for a user creation with Groups and Features with detached objects
     * Fail: If the User does not get created.
     * TODO returns an empty
     */
    @Test
    @Ignore
    public void testUserCreation() {
        User user = createUser("IntegrationTest");
        UserFeature userFeature = new UserFeature();
        Feature detachedFeature = new Feature();
        BeanUtils.copyProperties(messagingFeature, detachedFeature);
        userFeature.setFeature(detachedFeature);
        userFeature.setUser(user);
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userFeature);
        user = userRepository.save(user);

        Assert.assertFalse("The user's features should have been saved", CollectionUtils.isEmpty(user.getUserFeatures()));

    }

    private User createUser(String name) {
        User user = new User();
        user.setUsername(name);
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setDummy(Boolean.FALSE);
        user.setStartDate(new Date());
        user.setName(name);
        user.setEmail("test@patientview.org");
        userRepository.save(user);
        return user;
    }


    private User createSystemUser() {
        User user = new User();
        user.setUsername("system");
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setDummy(Boolean.FALSE);
        user.setStartDate(new Date());
        user.setName("System User");
        user.setEmail("System@patientview.org");
        userRepository.save(user);
        return user;
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(RoleName.STAFF_ADMIN);
        role.setCreated(new Date());
        role.setCreator(systemUser);
        roleRepository.save(role);
        return role;
    }

    private Feature createFeature(String name) {
        Feature feature = new Feature();
        feature.setName(name);
        feature.setCreated(new Date());
        feature.setCreator(systemUser);
        featureRepository.save(feature);
        return feature;
    }


}
