package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/08/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class UserRepositoryTest {

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void findByGroupsRoles() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);
        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findByGroupsRoles("%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findByGroupsRolesWithIdentifier() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);
        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("1111111111");
        identifier.setIdentifierType(lookup);
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Page<User> users = userRepository.findByGroupsRoles("%" + identifier.getIdentifier() + "%",
                Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findByGroupsRolesFeatures() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);

        Feature feature = dataTestUtils.createFeature("MESSAGING");
        UserFeature userFeature = new UserFeature();
        userFeature.setId(1L);
        userFeature.setUser(user);
        userFeature.setFeature(feature);
        userFeature.setCreator(creator);
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userFeature);
        userRepository.save(user);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};
        Long[] featureIdsArr = {feature.getId()};

        Page<User> users = userRepository.findByGroupsRolesFeatures("%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), Arrays.asList(featureIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findByGroupsRolesFeaturesWithIdentifier() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);

        Feature feature = dataTestUtils.createFeature("MESSAGING");
        UserFeature userFeature = new UserFeature();
        userFeature.setId(1L);
        userFeature.setUser(user);
        userFeature.setFeature(feature);
        userFeature.setCreator(creator);
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userFeature);

        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("1111111111");
        identifier.setIdentifierType(lookup);
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};
        Long[] featureIdsArr = {feature.getId()};

        Page<User> users = userRepository.findByGroupsRolesFeatures("%" + identifier.getIdentifier() + "%",
                Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr), Arrays.asList(featureIdsArr),
                new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findByGroupAndFeature() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        GroupRole groupRole = dataTestUtils.createGroupRole(user, group, role);

        Feature feature = dataTestUtils.createFeature("UNIT_TECHNICAL_CONTACT");
        UserFeature userFeature = new UserFeature();
        userFeature.setId(1L);
        userFeature.setUser(user);
        userFeature.setFeature(feature);
        userFeature.setCreator(creator);
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userFeature);
        userRepository.save(user);

        Iterable<User> users = userRepository.findByGroupAndFeature(group, feature);

        Assert.assertTrue("Should be one user returned", users.iterator().hasNext());

    }
}
