package org.patientview.test.util;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.RoleType;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.RouteLink;
import org.patientview.persistence.model.SimpleAuditModel;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserInformationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
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
    UserInformationRepository userInformationRepository;

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

    @Inject
    CodeRepository codeRepository;

    @Inject
    ObservationHeadingRepository observationHeadingRepository;

    User creator;

    @PostConstruct
    public void init() {
        User user = TestUtils.createUser("testCreator");
        user.setId(null);
        creator = userRepository.save(user);
    }

    private void setupBaseObject(SimpleAuditModel simpleAuditModel) {
        simpleAuditModel.setCreator(creator);
        simpleAuditModel.setId(null);
    }

    public Lookup createLookup(String lookupName, LookupTypes lookupTypeName) {

        LookupType lookupType = TestUtils.createLookupType(lookupTypeName);
        setupBaseObject(lookupType);
        lookupTypeRepository.save(lookupType);

        Lookup lookupValue = TestUtils.createLookup(lookupType, lookupName);
        setupBaseObject(lookupValue);
        return lookupRepository.save(lookupValue);

    }

    public User createUser(String username) {
        User user = TestUtils.createUser(username);
        setupBaseObject(user);
        return userRepository.save(user);
    }

    public UserInformation createUserInformation(User user, UserInformationTypes informationType, String value) {
        UserInformation userInformation = TestUtils.createUserInformation(user, informationType, value);
        setupBaseObject(userInformation);
        return userInformationRepository.save(userInformation);
    }


    public Feature createFeature(String name) {
        Feature feature = TestUtils.createFeature(name);
        setupBaseObject(feature);
        return featureRepository.save(feature);
    }

    public Role createRole(RoleName name, org.patientview.persistence.model.enums.RoleType roleTypeEnum) {
        Role role = TestUtils.createRole(name);
        setupBaseObject(role);
        RoleType roleType = new RoleType();
        roleType.setValue(roleTypeEnum);
        role.setRoleType(roleType);
        role.setVisible(true);

        return roleRepository.save(role);
    }

    public Role createRole(RoleName name, org.patientview.persistence.model.enums.RoleType roleTypeEnum,
                           boolean visible) {
        Role role = TestUtils.createRole(name);
        setupBaseObject(role);
        RoleType roleType = new RoleType();
        roleType.setValue(roleTypeEnum);
        role.setRoleType(roleType);
        role.setVisible(visible);

        return roleRepository.save(role);
    }


    public Group createGroup(String name) {
        Group group = TestUtils.createGroup(name);
        setupBaseObject(group);
        return groupRepository.save(group);
    }


    public GroupRole createGroupRole(User user, Group group, Role role) {
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        setupBaseObject(groupRole);
        return groupRoleRepository.save(groupRole);
    }

    public GroupRelationship createGroupRelationship(Group source, Group object, RelationshipTypes relationshipType) {
        GroupRelationship groupRelationship = TestUtils.createGroupRelationship(source, object, relationshipType);
        setupBaseObject(groupRelationship);
        return groupRelationshipRepository.save(groupRelationship);
    }

    public Route createRoute(String title, String controller, Lookup lookup) {
        Route route = TestUtils.createRoute(title, controller, lookup);
        setupBaseObject(route);
        return routeRepository.save(route);

    }

    public Route createRouteLink(Route route, Role role, Feature feature, Group group) {
        RouteLink routeLink = TestUtils.createRouteLink(route, role, group, feature);
        setupBaseObject(routeLink);
        if (CollectionUtils.isEmpty(route.getRouteLinks())) {
            route.setRouteLinks(new HashSet<RouteLink>());
        }

        route.getRouteLinks().add(routeLink);
        return routeRepository.save(route);

    }

    public Code createCode (String code, String description, String codeType, String standardType) {
        Code newCode = new Code();
        setupBaseObject(newCode);
        newCode.setCode(code);
        newCode.setDescription(description);
        newCode.setCodeType(createLookup(codeType, LookupTypes.CODE_TYPE));
        newCode.setStandardType(createLookup(standardType, LookupTypes.CODE_STANDARD));
        return codeRepository.save(newCode);
    }

    public ObservationHeading createObservationHeading(String code) {
        ObservationHeading observationHeading = TestUtils.createObservationHeading(code);
        return observationHeadingRepository.save(observationHeading);
    }
}
