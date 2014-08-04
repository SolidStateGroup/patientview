package org.patientview.test.util;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.RouteLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.Roles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Id can be passed when not using any persistence to test against.
 *
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public final class TestUtils {

    private TestUtils() {

    }

    public static User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setDummy(Boolean.FALSE);
        user.setStartDate(new Date());
        user.setName(name);
        user.setEmail("test@patientview.org");
        user.setEmailVerified(true);
        user.setPassword("doNotShow");
        return user;
    }

    public static Role createRole(Long id, Roles name, User creator) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setCreated(new Date());
        role.setCreator(creator);
        return role;
    }


    public static Feature createFeature(Long id, String name, User creator) {
        Feature feature = new Feature();
        feature.setId(id);
        feature.setName(name);
        feature.setCreated(new Date());
        feature.setCreator(creator);
        return feature;
    }

    public static Group createGroup(Long id, String name, User creator) {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setCreated(new Date());
        group.setCreator(creator);
        group.setVisible(true);
        group.setVisibleToJoin(true);
        return group;
    }

    public static GroupRole createGroupRole(Long id, Role role, Group group, User user, User creator) {
        GroupRole groupRole = new GroupRole();
        groupRole.setId(id);
        groupRole.setCreated(new Date());
        groupRole.setGroup(group);
        groupRole.setUser(user);
        groupRole.setRole(role);
        groupRole.setCreator(creator);
        return groupRole;
    }

    public static GroupFeature createGroupFeature(Long id, Feature feature, Group group, User creator) {
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setId(id);
        groupFeature.setCreated(new Date());
        groupFeature.setFeature(feature);
        groupFeature.setGroup(group);
        groupFeature.setCreator(creator);
        return groupFeature;

    }

    public static UserFeature createUserFeature(Long id, Feature feature, User user, User creator) {
        UserFeature userFeature = new UserFeature();
        userFeature.setId(id);
        userFeature.setCreated(new Date());
        userFeature.setFeature(feature);
        userFeature.setUser(user);
        userFeature.setCreator(creator);
        return userFeature;

    }

    public static Route createRoute(Long id, String title, String controller, Lookup lookup) {
        Route route = new Route();
        route.setTitle(title);
        route.setController(controller);
        route.setId(id);
        route.setDisplayOrder(1);
        route.setUrl("/test/url");
        route.setTemplateUrl("/test/url");
        route.setLookup(lookup);
        route.setCreated(new Date());
        return route;
    }

    public static RouteLink createRouteLink(Long id, Route route, Role role, Group group, Feature feature, User creator) {
        RouteLink routeLink = new RouteLink();
        routeLink.setId(id);
        routeLink.setRoute(route);
        routeLink.setRole(role);
        routeLink.setCreator(creator);
        routeLink.setFeature(feature);
        routeLink.setGroup(group);
        return routeLink;

    }

    public static Lookup createLookup(Long id, LookupType lookupType, String lookupName, User creator) {

        Lookup lookup = new Lookup();
        lookup.setId(id);
        lookup.setLookupType(lookupType);
        lookup.setValue(lookupName);
        lookup.setCreated(new Date());
        lookup.setCreator(creator);
        return lookup;
    }

    public static LookupType createLookupType(Long id, String type, User creator) {

        LookupType lookupType = new LookupType();
        lookupType.setId(id);
        lookupType.setType(LookupTypes.ROLE);
        lookupType.setCreated(new Date());
        lookupType.setCreator(creator);
        return lookupType;
    }

    public static Identifier createIdentifier(Long id, Lookup identifierType, User user, User creator) {
        Identifier identifier = new Identifier();
        identifier.setId(id);
        identifier.setIdentifierType(identifierType);
        identifier.setUser(user);
        identifier.setCreated(new Date());
        identifier.setCreator(creator);
        return identifier;
    }

    public static GroupRelationship createGroupRelationship(Long id, Group source, Group object
            , RelationshipTypes relationshipType, User creator) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setId(id);
        groupRelationship.setStartDate(new Date());
        groupRelationship.setCreator(creator);
        groupRelationship.setRelationshipType(relationshipType);
        groupRelationship.setObjectGroup(object);
        groupRelationship.setSourceGroup(source);

        return groupRelationship;

    }

    public static Code createCode(Long id, String description, User creator) {
        Code code = new Code();
        code.setId(id);
        code.setDisplayOrder(1);
        code.setDescription(description);
        code.setCreator(creator);
        return code;
    }

    public static Link createLink(Long id, Code code, String name, Lookup linkType, User creator) {
        Link link = new Link();
        link.setId(id);
        link.setLinkType(linkType);
        link.setCode(code);
        link.setLink(name);
        link.setCreator(creator);
        link.setDisplayOrder(1);
        return link;
    }

    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;

    }

    public static void authenticateTest(User user, Collection<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (Role role : roles) {
            authorities.add(role);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
