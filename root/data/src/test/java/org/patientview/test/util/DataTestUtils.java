package org.patientview.test.util;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.RoleType;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.RouteLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashSet;

/**
 * Test utilities for testing with a Persistence Context.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class DataTestUtils {

    @Inject
    UserRepository userRepository;

    @Inject
    LookupRepository lookupRepository;

    @Inject
    LookupTypeRepository lookupTypeRepository;

    @Inject
    FeatureRepository featureRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    GroupRepository groupRepository;

    @Inject
    GroupRoleRepository groupRoleRepository;

    @Inject
    GroupRelationshipRepository groupRelationshipRepository;

    @Inject
    RouteRepository routeRepository;

    public Lookup createLookup(String lookupName, LookupTypes lookupTypeName, User creator) {

        LookupType lookupType = TestUtils.createLookupType(null, lookupTypeName, creator);
        lookupType.setCreator(creator);
        lookupTypeRepository.save(lookupType);

        Lookup lookupValue = TestUtils.createLookup(null,lookupType, lookupName,  creator);
        lookupValue.setCreator(creator);
        return lookupRepository.save(lookupValue);

    }

    public User createUser(String username) {
        User user = TestUtils.createUser(null, username);
        return userRepository.save(user);
    }

    public Feature createFeature(String name, User creator) {

        Feature feature = TestUtils.createFeature(null, name, creator);
        return featureRepository.save(feature);
    }

    public Role createRole(String name, User creator) {
        Role role = TestUtils.createRole(null, Roles.PATIENT, creator);

        RoleType roleType = new RoleType();
        roleType.getValue();
        return roleRepository.save(role);
    }


    public Group createGroup(String name, User creator) {
        Group group = TestUtils.createGroup(null, name, creator);
        return groupRepository.save(group);
    }


    public GroupRole createGroupRole(User user, Group group, Role role, User creator) {
        GroupRole groupRole = TestUtils.createGroupRole(null, role, group, user, creator);
        return groupRoleRepository.save(groupRole);
    }

    public GroupRelationship createGroupRelationship(Group source, Group object, RelationshipTypes relationshipType, User creator) {
        GroupRelationship groupRelationship = TestUtils.createGroupRelationship(null, source, object, relationshipType, creator);
        return groupRelationshipRepository.save(groupRelationship);
    }

    public Route createRoute(String title, String controller, Lookup lookup, User creator) {
        Route route = TestUtils.createRoute(null, title, controller, lookup);
        return routeRepository.save(route);

    }

    public Route createRouteLink(Route route, Role role, Feature feature, Group group, User creator) {
        RouteLink routeLink = TestUtils.createRouteLink(null, route, role, group, feature, creator);

        if (CollectionUtils.isEmpty(route.getRouteLinks())) {
            route.setRouteLinks(new HashSet<RouteLink>());
        }

        route.getRouteLinks().add(routeLink);
        return routeRepository.save(route);

    }



}
