package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.UserServiceImpl;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
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

    @Mock
    private IdentifierRepository identifierRepository;

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
        User creator = TestUtils.createUser(1L, "testCreateUser");
        User newUser = TestUtils.createUser(2L, "newTestUser");
        Feature feature = TestUtils.createFeature(3L, "TEST_FEATURE", creator);

        // Add test feature
        UserFeature userFeature = TestUtils.createUserFeature(4L, feature, newUser, creator);
        newUser.setUserFeatures(new HashSet<UserFeature>());
        newUser.getUserFeatures().add(userFeature);

        // Add test role group
        Role role = TestUtils.createRole(5L, "TEST_ROLE", creator);
        Group group = TestUtils.createGroup(6L, "TEST_GROUP", creator);
        GroupRole groupRole = TestUtils.createGroupRole(7L, role, group, newUser, creator);
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.getGroupRoles().add(groupRole);

        // Add test identifier, with lookup type IDENTIFIER, value NHS_NUMBER
        LookupType lookupType = TestUtils.createLookupType(8L, "IDENTIFIER", creator);
        Lookup lookup = TestUtils.createLookup(9L, lookupType, "NHS_NUMBER", creator);
        Identifier identifier = TestUtils.createIdentifier(10L, lookup, newUser, creator);
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.getIdentifiers().add(identifier);

        when(userRepository.save(Matchers.eq(newUser))).thenReturn(newUser);

        userService.createUserWithPasswordEncryption(newUser);

        verify(userFeatureRepository, Mockito.times(1)).save(Matchers.eq(userFeature));
        verify(groupRoleRepository, Mockito.times(1)).save(Matchers.eq(groupRole));
        verify(identifierRepository, Mockito.times(1)).save(Matchers.eq(identifier));

    }



}
