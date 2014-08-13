package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class RouteRepositoryTest {

    @Inject
    RouteRepository routeRepository;

    @Inject
    UserFeatureRepository userFeatureRepository;

    @Inject
    GroupRoleRepository groupRoleRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    Lookup lookup;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
        lookup = dataTestUtils.createLookup("TOP", LookupTypes.MENU, creator);

    }


    /**
     * Test: Create a feature, link the user and the route to that feature
     * Fail: The feature should be accessed via a query for feature routes
     *
     */
    @Test
    public void testGetFeatureRoutes() {

        // Create the feature for the user and the route to link to
        Feature feature = dataTestUtils.createFeature("TEST_FEATURE", creator);

        // Create route with the feature attached
        Route route = dataTestUtils.createRoute("testRoute", "testController", lookup, creator);
        route = dataTestUtils.createRouteLink(route, null, feature , null, creator);


        // Create the user that should have the route
        User routeUser = dataTestUtils.createUser("testRouter");

        // Create the link to the feature for the user
        UserFeature userFeature = TestUtils.createUserFeature(null, feature, routeUser, creator);
        userFeatureRepository.save(userFeature);

        Iterable<Route> routes = routeRepository.findFeatureRoutesByUser(routeUser);
        Iterator<Route> iterator = routes.iterator();

        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be a route available", iterator.hasNext());
        Assert.assertTrue("The route should be the one created", iterator.next().equals(route));

    }

    /**
     * Test: Create a role, link the user and the route to that feature
     * Fail: The route should be accessed via a query for role routes
     *
     */
    @Test
    public void testGetRoleRoutes() {

        // Create the role for the user and the route to link to
        Role role = dataTestUtils.createRole("TEST_FEATURE", creator);


        // Create route with the role attached
        Route route = dataTestUtils.createRoute("testRoute", "testController", lookup, creator);
        route = dataTestUtils.createRouteLink(route, role, null , null, creator);

        // Create the user that should have the route
        User routeUser = dataTestUtils.createUser("testRouter");

        // Create the link to the role for the user
        Group group = dataTestUtils.createGroup("TEST_GROUP", creator);

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(routeUser);
        groupRole.setRole(role);
        groupRole.setGroup(group);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);


        Iterable<Route> routes = routeRepository.findRoleRoutesByUser(routeUser);
        Iterator<Route> iterator = routes.iterator();

        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be a route available", iterator.hasNext());
        Assert.assertTrue("The route should be the one created", iterator.next().equals(route));

    }

    /**
     * Test: Create a group, link the user and the route to that feature
     * Fail: The Route should be accessed via a query for group routes
     *
     */
    @Test
    public void testGetGroupRoutes() {

        // Create the group for the user and the route to link to
        Group group = dataTestUtils.createGroup("TEST_GROUP", creator);


        // Create route with the role attached
        Route route = dataTestUtils.createRoute("testRoute", "testController", lookup, creator);
        route = dataTestUtils.createRouteLink(route, null, null , group, creator);

        // Create the user that should have the route
        User routeUser = dataTestUtils.createUser("testRouter");

        // Create the link to the group for the user
        Role role = dataTestUtils.createRole("TEST_FEATURE", creator);

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(routeUser);
        groupRole.setRole(role);
        groupRole.setGroup(group);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);


        Iterable<Route> routes = routeRepository.findGroupRoutesByUser(routeUser);
        Iterator<Route> iterator = routes.iterator();

        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be a route available", iterator.hasNext());
        Assert.assertTrue("The route should be the one created", iterator.next().equals(route));

    }





}
