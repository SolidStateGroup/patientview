package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */


public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;


    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private UserFeatureRepository userFeatureRepository;

    @InjectMocks
    private UserService userService = new UserServiceImpl();


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test: The creation of the user with user features, groups and roles
     * Fail: The creation of the user fails without creating groups or user features
     *
     */
    @Test
    public void testCreateUser() {
        User user = TestUtils.createUser(123324L, "testCreateUser");

        // Add test feature
        UserFeature userFeature = TestUtils.createUserFeature(23423L, "testFeature", user);
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userFeature);

        // Add test role group
        GroupRole groupRole = TestUtils.createGroupRole(555L, "PATIENT", "testGroup", user);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(groupRole);

        when(userRepository.save(Matchers.eq(user))).thenReturn(user);

        userService.createUser(user);

        verify(userFeatureRepository, Mockito.times(1)).save(Matchers.eq(userFeature));
        verify(groupRoleRepository, Mockito.times(1)).save(Matchers.eq(groupRole));

    }



}
