package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.SecurityServiceImpl;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests concerning the test of the user security service for retrieving data that the user has access too.
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public class SecurityServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityService securityService = new SecurityServiceImpl();


    private User creator;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    /**
     * Test: To return routes that do not contain a list of duplicates
     * Fail: Duplicate routes are returning by the function
     */
    @Test
    public void testGetNoneDuplicateRoutes() {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        when(routeRepository.findFeatureRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.findGroupRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.findRoleRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());

        Set<Route> routes = securityService.getUserRoutes(testUser.getId());

        verify(routeRepository, Mockito.times(1)).findGroupRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).findFeatureRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).findRoleRoutesByUser(Matchers.eq(testUser));
        Assert.assertNotNull(routes);
        Assert.assertEquals("There should be only 6 routes", routes.size(), 6);
    }

    /**
     * Test: Call the find all method if a User has the superadmin role
     * Fail: Find All was not called (SuperAdmin -> findAll, Anyone else -> findGroupByUser)
     *
     * @return
     */
    @Test
    public void testGetUserGroupsWithSuperAdmin() {

        GetParameters getParameters = new GetParameters();
        User testUser = TestUtils.createUser("testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        List<Role> roles = new ArrayList<>();
        roles.add(TestUtils.createRole(RoleName.GLOBAL_ADMIN));
        when(roleRepository.findByUser(Matchers.eq(testUser))).thenReturn(roles);

        TestUtils.authenticateTest(testUser, RoleName.GLOBAL_ADMIN);
        securityService.getUserGroups(testUser.getId(), getParameters);

        String filterText = "%%";
        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        verify(groupRepository, Mockito.times(1)).findAll(filterText, pageable);
    }

    /**
     * Test: Call the findGroupByUser method if a User does not have a globaladmin role
     * Fail: FindGroupByUser was not called (SuperAdmin -> findAll, Anyone else -> findGroupByUser)
     *
     * @return
     */
    @Test
    public void testGetUserGroups() {
        GetParameters getParameters = new GetParameters();
        User testUser = TestUtils.createUser("testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        List<Role> roles = new ArrayList<>();
        roles.add(TestUtils.createRole(RoleName.UNIT_ADMIN));
        when(roleRepository.findValidRolesByUser(Matchers.eq(testUser.getId()))).thenReturn(roles);

        TestUtils.authenticateTest(testUser, RoleName.UNIT_ADMIN);
        securityService.getUserGroups(testUser.getId(), getParameters);

        String filterText = "%%";
        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        verify(groupRepository, Mockito.times(1)).findGroupsByUserNoSpecialties(filterText, testUser, pageable);
    }

    private Iterable<Route> getRoutes() {
        Set<Route> routes = new HashSet<>();
        for (long i = 1; i< 7; i ++) {
            Route route = new Route();
            route.setId(i);
            route.setTitle("Route" + i);
            route.setDisplayOrder((int) i);
            routes.add(route);
        }
        return routes;
    }

}
