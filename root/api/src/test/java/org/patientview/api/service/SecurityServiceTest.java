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
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

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
    private UserRepository userRepository;

    @Mock
    private NewsItemRepository newsItemRepository;

    @InjectMocks
    private SecurityService securityService = new SecurityServiceImpl();


    private User creator;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser(1L, "creator");
    }


    /**
     * Test: To see if the news is return by single group OR role
     * Fail: The calls to the repository are not made
     */
    @Test
    public void testGetNewsByUser() {

        User testUser = TestUtils.createUser(23L, "testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);

        securityService.getNewsByUser(testUser.getId());

        verify(newsItemRepository, Mockito.times(1)).getGroupNewsByUser(Matchers.eq(testUser));
        verify(newsItemRepository, Mockito.times(1)).getRoleNewsByUser(Matchers.eq(testUser));
    }

    /**
     * Test: To return routes that do not contain a list of duplicates
     * Fail: Duplicate routes are returning by the function
     */
    @Test
    public void testGetNoneDuplicateRoutes() {
        User testUser = TestUtils.createUser(23L, "testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        when(routeRepository.getFeatureRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.getGroupRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.getRoleRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());

        Set<Route> routes = securityService.getUserRoutes(1L);

        verify(routeRepository, Mockito.times(1)).getGroupRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).getFeatureRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).getRoleRoutesByUser(Matchers.eq(testUser));
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
        User testUser = TestUtils.createUser(23L, "testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        List<Role> roles = new ArrayList<Role>();
        roles.add(TestUtils.createRole(1L, "SUPER_ADMIN", creator));
        when(roleRepository.getValidRolesByUser(Matchers.eq(testUser.getId()))).thenReturn(roles);

        securityService.getUserGroups(testUser.getId());

        verify(groupRepository, Mockito.times(1)).findAll();

    }


    /**
     * Test: Call the findGroupByUser method if a User does not have a superadmin role
     * Fail: FindGroupByUser was not called (SuperAdmin -> findAll, Anyone else -> findGroupByUser)
     *
     * @return
     */
    @Test
    public void testGetUserGroups() {
        User testUser = TestUtils.createUser(23L, "testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        List<Role> roles = new ArrayList<Role>();
        roles.add(TestUtils.createRole(1L, "SUPER_ADMIN", creator));
        when(roleRepository.getValidRolesByUser(Matchers.eq(testUser.getId()))).thenReturn(roles);

        securityService.getUserGroups(testUser.getId());

        verify(groupRepository, Mockito.times(1)).findGroupByUser(testUser);

    }

    private Iterable<Route> getRoutes() {
        Set<Route> routes = new HashSet<Route>();
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
