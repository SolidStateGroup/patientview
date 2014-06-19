package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;

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

    @InjectMocks
    private UserService userService = new UserServiceImpl();


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test: The creation of the user with user features, groups and roles
     * Fail: The creation of the user fails
     *
     */
    @Test
    public void testCreateUser() {
        System.out.println("NOt implemented");
    }



}
