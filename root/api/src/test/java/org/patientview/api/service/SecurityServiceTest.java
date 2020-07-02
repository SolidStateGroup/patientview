package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.SecurityServiceImpl;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Optional;
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

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: To return routes that do not contain a list of duplicates
     * Fail: Duplicate routes are returning by the function
     */
    @Test
    public void testGetNoneDuplicateRoutes() {
        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);
        when(userRepository.findById(Matchers.anyLong())).thenReturn(Optional.of(testUser));
        when(routeRepository.findFeatureRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.findGroupRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());
        when(routeRepository.findRoleRoutesByUser(Matchers.any(User.class))).thenReturn(getRoutes());

        Set<Route> routes = securityService.getUserRoutes(testUser);

        verify(routeRepository, Mockito.times(1)).findGroupRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).findFeatureRoutesByUser(Matchers.eq(testUser));
        verify(routeRepository, Mockito.times(1)).findRoleRoutesByUser(Matchers.eq(testUser));
        Assert.assertNotNull(routes);
        Assert.assertEquals("There should be only 6 routes", routes.size(), 6);
    }

    private Iterable<Route> getRoutes() {
        Set<Route> routes = new HashSet<>();
        for (long i = 1; i < 7; i++) {
            Route route = new Route();
            route.setId(i);
            route.setTitle("Route" + i);
            route.setDisplayOrder((int) i);
            routes.add(route);
        }
        return routes;
    }

}
