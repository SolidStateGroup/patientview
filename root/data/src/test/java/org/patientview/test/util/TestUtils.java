package org.patientview.test.util;

import org.patientview.persistence.model.DonorStageData;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.RouteLink;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RequestTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.model.enums.StageTypes;
import org.patientview.persistence.model.enums.StageStatuses;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * Id can be passed when not using any persistence to test against.
 * <p/>
 * <p/>
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public final class TestUtils {

    final static User creator;

    static {
        creator = createUser("testCreator");
    }

    private TestUtils() {

    }

    private static Long getId() {
        long range = 1234567L;
        Random r = new Random();
        return (long) (r.nextDouble() * range);
    }

    public static User createUser(String name) {
        User user = new User();
        user.setId(getId());
        user.setUsername(name);
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setDummy(Boolean.FALSE);
        user.setStartDate(new Date());
        user.setName(name);
        user.setForename("forename");
        user.setSurname("surname");
        user.setEmail("test@patientview.org");
        user.setEmailVerified(true);
        user.setPassword("doNotShow");
        user.setGroupRoles(new HashSet<GroupRole>());
        user.setDeleted(Boolean.FALSE);
        return user;
    }

    public static UserInformation createUserInformation(User user, UserInformationTypes informationType, String value) {
        UserInformation userInformation = new UserInformation();
        userInformation.setUser(user);
        userInformation.setId(getId());
        userInformation.setType(informationType);
        userInformation.setValue(value);
        return userInformation;
    }

    public static Role createRole(RoleName name) {
        Role role = new Role();
        role.setId(getId());
        role.setName(name);
        role.setCreated(new Date());
        role.setCreator(creator);
        return role;
    }

    public static Role createRole(RoleName name, RoleType roleType) {
        org.patientview.persistence.model.RoleType roleTypeObj = new org.patientview.persistence.model.RoleType();
        roleTypeObj.setValue(roleType);

        Role role = new Role();
        role.setId(getId());
        role.setRoleType(roleTypeObj);
        role.setName(name);
        role.setCreated(new Date());
        role.setCreator(creator);
        return role;
    }


    public static Feature createFeature(String name) {
        Feature feature = new Feature();
        feature.setId(getId());
        feature.setName(name);
        feature.setCreated(new Date());
        feature.setCreator(creator);
        return feature;
    }

    public static Group createGroup(String name) {
        Group group = new Group();
        group.setId(getId());
        group.setCode(name.toUpperCase());
        group.setName(name);
        group.setCreated(new Date());
        group.setCreator(creator);
        group.setVisible(true);
        group.setVisibleToJoin(true);
        group.setGroupRelationships(new HashSet<GroupRelationship>());
        group.setGroupFeatures(new HashSet<GroupFeature>());
        return group;
    }

    public static GroupRole createGroupRole(Role role, Group group, User user) {
        GroupRole groupRole = new GroupRole();
        groupRole.setId(getId());
        groupRole.setCreated(new Date());
        groupRole.setGroup(group);
        groupRole.setUser(user);
        groupRole.setRole(role);
        groupRole.setCreator(creator);
        return groupRole;
    }

    public static GroupFeature createGroupFeature(Feature feature, Group group) {
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setId(getId());
        groupFeature.setCreated(new Date());
        groupFeature.setFeature(feature);
        groupFeature.setGroup(group);
        groupFeature.setCreator(creator);
        return groupFeature;

    }

    public static UserFeature createUserFeature(Feature feature, User user) {
        UserFeature userFeature = new UserFeature();
        userFeature.setId(getId());
        userFeature.setCreated(new Date());
        userFeature.setFeature(feature);
        userFeature.setUser(user);
        userFeature.setCreator(creator);
        return userFeature;

    }

    public static Route createRoute(String title, String controller, Lookup lookup) {
        Route route = new Route();
        route.setTitle(title);
        route.setController(controller);
        route.setId(getId());
        route.setDisplayOrder(1);
        route.setUrl("/test/url");
        route.setTemplateUrl("/test/url");
        route.setLookup(lookup);
        route.setCreated(new Date());
        return route;
    }

    public static RouteLink createRouteLink(Route route, Role role, Group group, Feature feature) {
        RouteLink routeLink = new RouteLink();
        routeLink.setId(getId());
        routeLink.setRoute(route);
        routeLink.setRole(role);
        routeLink.setCreator(creator);
        routeLink.setFeature(feature);
        routeLink.setGroup(group);
        return routeLink;

    }

    public static Lookup createLookup(LookupType lookupType, String lookupName) {

        Lookup lookup = new Lookup();
        lookup.setId(getId());
        lookup.setLookupType(lookupType);
        lookup.setValue(lookupName);
        lookup.setCreated(new Date());
        lookup.setCreator(creator);
        return lookup;
    }

    public static Lookup createLookup(LookupType lookupType, String lookupName, String lookupDescription) {

        Lookup lookup = new Lookup();
        lookup.setId(getId());
        lookup.setLookupType(lookupType);
        lookup.setValue(lookupName);
        lookup.setDescription(lookupDescription);
        lookup.setCreated(new Date());
        lookup.setCreator(creator);
        return lookup;
    }

    public static LookupType createLookupType(LookupTypes type) {

        LookupType lookupType = new LookupType();
        lookupType.setId(getId());
        lookupType.setType(type);
        lookupType.setCreated(new Date());
        lookupType.setCreator(creator);
        lookupType.setLookups(new HashSet<Lookup>());
        return lookupType;
    }

    public static Identifier createIdentifier(Lookup identifierType, User user, String value) {
        Identifier identifier = new Identifier();
        identifier.setId(getId());
        identifier.setIdentifierType(identifierType);
        identifier.setUser(user);
        identifier.setIdentifier(value);
        identifier.setCreated(new Date());
        identifier.setCreator(creator);

        if (CollectionUtils.isEmpty(user.getIdentifiers())) {
            user.setIdentifiers(new HashSet<Identifier>());
        }
        user.getIdentifiers().add(identifier);
        return identifier;
    }

    public static GroupRelationship createGroupRelationship(Group source, Group object
            , RelationshipTypes relationshipType) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setId(getId());
        groupRelationship.setStartDate(new Date());
        groupRelationship.setCreator(creator);
        groupRelationship.setRelationshipType(relationshipType);
        groupRelationship.setObjectGroup(object);
        groupRelationship.setSourceGroup(source);

        return groupRelationship;

    }

    public static Code createCode(String description) {
        Code code = new Code();
        code.setId(getId());
        code.setDisplayOrder(1);
        code.setDescription(description);
        code.setCreator(creator);
        return code;
    }

    public static Code createCode(String codeString, String description) {
        Code code = new Code();
        code.setCode(codeString);
        code.setId(getId());
        code.setDisplayOrder(1);
        code.setDescription(description);
        code.setCreator(creator);
        return code;
    }

    public static Link createLink(Group group, Code code, String name) {
        Link link = new Link();
        link.setId(getId());
        link.setGroup(group);
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

    public static void authenticateTest(User user, RoleName... roleNames) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Group group = createGroup("AuthenticationGroup");
        for (RoleName roleName : roleNames) {
            authorities.add(createGroupRole(createRole(roleName), group, user));
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void authenticateTest(User user, Collection<GroupRole> groupRoles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (GroupRole groupRole : groupRoles) {
            authorities.add(groupRole);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void authenticateTest(User user, Set<GroupRole> groupRoles) {
        if (groupRoles != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), groupRoles);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), Collections.EMPTY_SET);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    public static void authenticateTestSingleGroupRole(String userName, String groupName, RoleName roleName) {

        // create user, group role
        Group group = createGroup(groupName);
        Role role = createRole(roleName);
        User user = createUser(userName);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // authenticate
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getId(), groupRoles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated (p)");
        }

        return (User) authentication.getPrincipal();
    }

    public static void removeAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public static GroupStatistic createGroupStatistics(Group group, BigInteger value, Lookup lookup) {
        GroupStatistic groupStatistic = new GroupStatistic();
        groupStatistic.setGroup(group);
        groupStatistic.setId(getId());
        groupStatistic.setValue(value);
        groupStatistic.setStatisticType(lookup);
        groupStatistic.setEndDate(new Date());
        groupStatistic.setStartDate(new Date());
        groupStatistic.setStatisticPeriod(StatisticPeriod.MONTH);
        return groupStatistic;
    }

    public static NewsItem createNewsItem(String heading, String story) {
        NewsItem newsItem = new NewsItem();
        newsItem.setId(getId());
        newsItem.setCreator(creator);
        newsItem.setHeading(heading);
        newsItem.setStory(story);
        newsItem.setNewsLinks(new HashSet<NewsLink>());
        newsItem.setCreated(new Date());
        return newsItem;
    }

    public static NewsLink createNewsLink(NewsItem newsItem, Group group, Role role) {
        NewsLink newsLink = new NewsLink();
        newsLink.setId(getId());
        newsLink.setNewsItem(newsItem);
        newsLink.setGroup(group);
        newsLink.setRole(role);
        newsLink.setCreator(creator);
        return newsLink;
    }

    public static Request createRequest(Group group, RequestStatus status, RequestTypes type) {

        Request request = new Request();
        request.setCreated(new Date());
        request.setStatus(status);
        request.setGroup(group);
        request.setNhsNumber("234234234");
        request.setDateOfBirth(new Date());
        request.setType(type);
        return request;
    }

    public static ContactPoint createContactPoint(String value, ContactPointTypes contactPointTypes) {
        ContactPointType contactPointType = new ContactPointType();
        contactPointType.setId(getId());
        contactPointType.setLookupType(createLookupType(LookupTypes.CONTACT_POINT_TYPE));
        contactPointType.setValue(contactPointTypes);

        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setContent(value);
        contactPoint.setContactPointType(contactPointType);
        contactPoint.setId(getId());
        contactPoint.setCreated(new Date());
        contactPoint.setCreator(creator);

        return contactPoint;
    }

    public static FhirLink createFhirLink(User user, Identifier identifier) {
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setResourceId(UUID.randomUUID());
        fhirLink.setVersionId(UUID.randomUUID());
        fhirLink.setCreated(new Date());
        fhirLink.setIdentifier(identifier);
        fhirLink.setActive(true);
        if (CollectionUtils.isEmpty(user.getFhirLinks())) {
            user.setFhirLinks(new HashSet<FhirLink>());
        }

        user.getFhirLinks().add(fhirLink);
        return fhirLink;
    }

    public static FhirLink createFhirLink(User user, Identifier identifier, Group group) {
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setResourceId(UUID.randomUUID());
        fhirLink.setVersionId(UUID.randomUUID());
        fhirLink.setCreated(new Date());
        fhirLink.setIdentifier(identifier);
        fhirLink.setActive(true);
        fhirLink.setGroup(group);
        if (CollectionUtils.isEmpty(user.getFhirLinks())) {
            user.setFhirLinks(new HashSet<FhirLink>());
        }

        user.getFhirLinks().add(fhirLink);
        return fhirLink;
    }

    public static ObservationHeading createObservationHeading(String code) {
        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setCode(code);
        //observationHeading.setCreated(new Date());
        //observationHeading.setCreator(creator);
        return observationHeading;
    }
}
