package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
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
import java.util.List;

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
    public void emailExists() {
        User user = dataTestUtils.createUser("testUser");
        user.setEmail("test@solidstategroup.com");
        userRepository.save(user);
        Assert.assertTrue("Email should exist", userRepository.emailExists(user.getEmail()));
    }

    @Test
    public void emailExistsCaseInsensitive() {
        User user = dataTestUtils.createUser("testUser");
        user.setEmail("Test@solidstategroup.com");
        userRepository.save(user);
        Assert.assertTrue("Email should exist", userRepository.emailExistsCaseInsensitive("Test@solidstategroup.com"));
    }

    @Test
    public void findGroupTest() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        User user2 = dataTestUtils.createUser("test2User");
        Group group2 = dataTestUtils.createGroup("test2Group");

        User user3 = dataTestUtils.createUser("test3User");
        Group group3 = dataTestUtils.createGroup("test3Group");

        // yes
        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        // no
        dataTestUtils.createGroupRole(user2, group2, role);
        dataTestUtils.createGroupRole(user2, group3, role);

        // yes
        dataTestUtils.createGroupRole(user3, group, role);
        dataTestUtils.createGroupRole(user3, group2, role);
        dataTestUtils.createGroupRole(user3, group3, role);

        Long[] groupIdsArr = {group.getId(), group2.getId()};

        List<User> users = userRepository.findGroupTest(Arrays.asList(groupIdsArr), 2L);

        Assert.assertEquals("Should be 2 user returned", 2, users.size());
    }

    @Test
    public void findAllPatients() {
        User user = dataTestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        List<User> users = userRepository.findAllPatients();

        Assert.assertEquals("Should be one user returned", 1, users.size());
    }

    @Test
    public void findByEmail() {
        User user = dataTestUtils.createUser("testUser");
        user.setEmail("Test@solidstategroup.com");
        userRepository.save(user);
        List<User> foundUsers = userRepository.findByEmail(user.getEmail());

        Assert.assertEquals("Should return one user", 1, foundUsers.size());
        Assert.assertEquals("Should return correct user", user.getEmail(), foundUsers.get(0).getEmail());
    }

    @Test
    public void findByEmailCaseInsensitive() {
        User user = dataTestUtils.createUser("testUser");
        user.setEmail("Test@solidstategroup.com");
        userRepository.save(user);
        List<User> foundUsers = userRepository.findByEmailCaseInsensitive("TEST@solidSTATEgroup.com");

        Assert.assertEquals("Should return one user", 1, foundUsers.size());
        Assert.assertEquals("Should return correct user", user.getEmail(), foundUsers.get(0).getEmail());
    }

    @Test
    public void findByGroupAndFeature() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

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

    @Test
    public void findPatientByGroupsRoles() {
        User user = dataTestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findPatientByGroupsRolesAnd("%%", "%%", "%%", "%%", "%%",
                Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr), 1l, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findPatientByGroupsRoles_multiple() {
        User user = dataTestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        User user2 = dataTestUtils.createUser("testUser");
        user2.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier2 = new Identifier();
        identifier2.setIdentifier("test");
        identifier2.setUser(user2);
        user2.getIdentifiers().add(identifier2);
        userRepository.save(user2);

        User user3 = dataTestUtils.createUser("testUser");
        user3.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier3 = new Identifier();
        identifier3.setIdentifier("test");
        identifier3.setUser(user3);
        user3.getIdentifiers().add(identifier3);
        userRepository.save(user3);

        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Group group3 = dataTestUtils.createGroup("test3Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        // yes
        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        // no
        dataTestUtils.createGroupRole(user2, group2, role);
        dataTestUtils.createGroupRole(user2, group3, role);

        // yes
        dataTestUtils.createGroupRole(user3, group, role);
        dataTestUtils.createGroupRole(user3, group2, role);
        dataTestUtils.createGroupRole(user3, group3, role);

        Long[] groupIdsArr = {group.getId(), group2.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findPatientByGroupsRolesAnd("%%", "%%", "%%", "%%", "%%",
                Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr), 2l, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 2 user returned", 2, users.getContent().size());
    }

    @Test
    public void findPatientByGroupsRoles_searchUsername() {
        User user = dataTestUtils.createUser("testUser");
        user.setForename("forename");
        user.setSurname("surname");
        user.setEmail("email");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findPatientByGroupsRoles("%" + user.getUsername().toUpperCase() + "%", "%%",
                "%%", "%%", "%%", Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr),
                new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findPatientByGroupsRoles_searchUsernameAndSurname() {
        User user = dataTestUtils.createUser("testUser");
        user.setSurname("surnameExample");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        userRepository.save(user);

        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findPatientByGroupsRoles("%" + user.getUsername().toUpperCase() + "%", "%%",
                "%" + user.getSurname().toUpperCase() + "%", "%%", "%%", Arrays.asList(groupIdsArr),
                Arrays.asList(roleIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findPatientByGroupsRolesFeaturesWithIdentifier() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

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

        Page<User> users = userRepository.findPatientByGroupsRolesFeatures("%" + identifier.getIdentifier() + "%",
                Arrays.asList(groupIdsArr), Arrays.asList(roleIdsArr), Arrays.asList(featureIdsArr),
                new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findStaffByGroupsRoles() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findStaffByGroupsRolesAnd("%%", "%%", "%%", "%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), 1L, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void findStaffByGroupsRoles_deleted() {
        User user = dataTestUtils.createUser("testUser");
        user.setDeleted(true);
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long[] groupIdsArr = {group.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findStaffByGroupsRolesAnd("%%", "%%", "%%", "%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), 1L, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be no users returned", 0, users.getContent().size());
    }

    @Test
    public void findStaffByGroupsRoles_multiple() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        User user2 = dataTestUtils.createUser("test2User");
        Group group2 = dataTestUtils.createGroup("test2Group");

        User user3 = dataTestUtils.createUser("test3User");
        Group group3 = dataTestUtils.createGroup("test3Group");

        // yes
        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        // no
        dataTestUtils.createGroupRole(user2, group2, role);
        dataTestUtils.createGroupRole(user2, group3, role);

        // yes
        dataTestUtils.createGroupRole(user3, group, role);
        dataTestUtils.createGroupRole(user3, group2, role);
        dataTestUtils.createGroupRole(user3, group3, role);

        Long[] groupIdsArr = {group.getId(), group2.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findStaffByGroupsRolesAnd("%%", "%%", "%%", "%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), 2L, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 2 user returned", 2, users.getContent().size());
    }

    @Test
    public void findStaffByGroupsRoles_multiple_deleted() {
        User user = dataTestUtils.createUser("testUser");
        user.setDeleted(true);
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        User user2 = dataTestUtils.createUser("test2User");
        Group group2 = dataTestUtils.createGroup("test2Group");

        User user3 = dataTestUtils.createUser("test3User");
        Group group3 = dataTestUtils.createGroup("test3Group");

        // no (deleted)
        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        // no
        dataTestUtils.createGroupRole(user2, group2, role);
        dataTestUtils.createGroupRole(user2, group3, role);

        // yes
        dataTestUtils.createGroupRole(user3, group, role);
        dataTestUtils.createGroupRole(user3, group2, role);
        dataTestUtils.createGroupRole(user3, group3, role);

        Long[] groupIdsArr = {group.getId(), group2.getId()};
        Long[] roleIdsArr = {role.getId()};

        Page<User> users = userRepository.findStaffByGroupsRolesAnd("%%", "%%", "%%", "%%", Arrays.asList(groupIdsArr)
                , Arrays.asList(roleIdsArr), 2L, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 1 user returned", 1, users.getContent().size());
    }

    @Test
    public void findStaffByGroupsRolesFeatures() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

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

        Page<User> users = userRepository.findStaffByGroupsRolesFeatures("%%", Arrays.asList(groupIdsArr),
                Arrays.asList(roleIdsArr), Arrays.asList(featureIdsArr), new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one user returned", 1, users.getContent().size());
    }

    @Test
    public void usernameExists() {
        User user = dataTestUtils.createUser("testUser");
        userRepository.save(user);
        Assert.assertTrue("Username should exist", userRepository.usernameExistsCaseInsensitive(user.getUsername()));
    }
}
