package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.TransformerUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class FhirLinkRepositoryTest {

    @Inject
    FhirLinkRepository fhirLinkRepository;

    @Inject
    GroupRepository groupRepository;

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
    public void testFindByGroups() {
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("testGroup2");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);

        // group 1
        User user1 = dataTestUtils.createUser("testUser1");
        dataTestUtils.createGroupRole(user1, group, role);
        user1.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier1 = new Identifier();
        identifier1.setIdentifier("1");
        identifier1.setIdentifierType(lookup);
        identifier1.setUser(user1);
        user1.getIdentifiers().add(identifier1);
        identifierRepository.save(identifier1);

        FhirLink fhirLink1 = new FhirLink();
        fhirLink1.setUser(user1);
        fhirLink1.setGroup(group);
        fhirLink1.setIdentifier(identifier1);
        fhirLink1.setResourceId(UUID.fromString("e5294f9d-7122-4ba9-98eb-229f0426e0ad"));
        fhirLinkRepository.save(fhirLink1);

        user1.setFhirLinks(new HashSet<FhirLink>());
        user1.getFhirLinks().add(fhirLink1);
        userRepository.save(user1);

        // group 2
        User user2 = dataTestUtils.createUser("testuser2");
        dataTestUtils.createGroupRole(user2, group2, role);
        user2.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier2 = new Identifier();
        identifier2.setIdentifier("1");
        identifier2.setIdentifierType(lookup);
        identifier2.setUser(user2);
        user2.getIdentifiers().add(identifier2);
        identifierRepository.save(identifier2);

        FhirLink fhirLink2 = new FhirLink();
        fhirLink2.setUser(user2);
        fhirLink2.setGroup(group2);
        fhirLink2.setIdentifier(identifier2);
        fhirLink2.setResourceId(UUID.fromString("6b26fd1d-77df-4094-937a-866f66ae2a8e"));
        fhirLinkRepository.save(fhirLink2);

        user2.setFhirLinks(new HashSet<FhirLink>());
        user2.getFhirLinks().add(fhirLink2);
        userRepository.save(user2);

        List<FhirLink> fhirLinks = IteratorUtils.toList(fhirLinkRepository.findAll().iterator());
        Assert.assertEquals("There should be 2 FhirLink in total", 2, fhirLinks.size());

        List<Group> groups = new ArrayList<>();
        groups.add(group);

        fhirLinks = fhirLinkRepository.findByGroups(groups);
        Assert.assertEquals("There should be 1 FhirLink found by group", 1, fhirLinks.size());
        Assert.assertEquals("Should be correct FhirLink found by group",
                fhirLink1.getResourceId(), fhirLinks.get(0).getResourceId());

        // get uuids from list of FhirLink
        List<UUID> uuids = (List<UUID>) CollectionUtils.collect(fhirLinks,
                TransformerUtils.invokerTransformer("getResourceId"));
        Assert.assertEquals("Should be correct resourceId found by group", fhirLink1.getResourceId(), uuids.get(0));
    }

    @Test
    public void testFindByGroupsAndRecentLogin() {
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);

        // user 1 (last login 6 months ago)
        User user1 = dataTestUtils.createUser("testUser1");
        user1.setLastLogin(new DateTime(new Date()).minusMonths(6).toDate());
        dataTestUtils.createGroupRole(user1, group, role);
        user1.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier1 = new Identifier();
        identifier1.setIdentifier("1");
        identifier1.setIdentifierType(lookup);
        identifier1.setUser(user1);
        user1.getIdentifiers().add(identifier1);
        identifierRepository.save(identifier1);

        FhirLink fhirLink1 = new FhirLink();
        fhirLink1.setUser(user1);
        fhirLink1.setGroup(group);
        fhirLink1.setIdentifier(identifier1);
        fhirLink1.setResourceId(UUID.fromString("e5294f9d-7122-4ba9-98eb-229f0426e0ad"));
        fhirLinkRepository.save(fhirLink1);

        user1.setFhirLinks(new HashSet<FhirLink>());
        user1.getFhirLinks().add(fhirLink1);
        userRepository.save(user1);

        // user 2 (last login 2 months ago)
        User user2 = dataTestUtils.createUser("testuser2");
        user2.setCurrentLogin(new DateTime(new Date()).minusMonths(2).toDate());
        dataTestUtils.createGroupRole(user2, group, role);
        user2.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier2 = new Identifier();
        identifier2.setIdentifier("1");
        identifier2.setIdentifierType(lookup);
        identifier2.setUser(user2);
        user2.getIdentifiers().add(identifier2);
        identifierRepository.save(identifier2);

        FhirLink fhirLink2 = new FhirLink();
        fhirLink2.setUser(user2);
        fhirLink2.setGroup(group);
        fhirLink2.setIdentifier(identifier2);
        fhirLink2.setResourceId(UUID.fromString("6b26fd1d-77df-4094-937a-866f66ae2a8e"));
        fhirLinkRepository.save(fhirLink2);

        user2.setFhirLinks(new HashSet<FhirLink>());
        user2.getFhirLinks().add(fhirLink2);
        userRepository.save(user2);

        List<FhirLink> fhirLinks = IteratorUtils.toList(fhirLinkRepository.findAll().iterator());
        Assert.assertEquals("There should be 2 FhirLink in total", 2, fhirLinks.size());

        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();

        List<Group> groups = new ArrayList<>();
        groups.add(group);

        fhirLinks = fhirLinkRepository.findByGroupsAndRecentLogin(groups, threeMonthsAgo);
        Assert.assertEquals("There should be 1 FhirLink found by recent", 1, fhirLinks.size());
        Assert.assertEquals("Should be correct FhirLink found by recent",
                fhirLink2.getResourceId(), fhirLinks.get(0).getResourceId());

        // get uuids from list of FhirLink
        List<UUID> uuids = (List<UUID>) CollectionUtils.collect(fhirLinks,
                TransformerUtils.invokerTransformer("getResourceId"));
        Assert.assertEquals("Should be correct resourceId found by recent", fhirLink2.getResourceId(), uuids.get(0));
    }

    @Test
    public void testFindPatientCountWithoutFhirLink() {
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        // no fhirlink
        User user = dataTestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        identifierRepository.save(identifier);
        userRepository.save(user);

        // has fhirlink
        User user2 = dataTestUtils.createUser("testuser2");
        user2.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier2 = new Identifier();
        identifier2.setIdentifier("1");
        identifier2.setUser(user2);
        user2.getIdentifiers().add(identifier2);
        identifierRepository.save(identifier2);
        FhirLink fhirLink2 = new FhirLink();
        fhirLink2.setUser(user2);
        fhirLink2.setGroup(group2);
        fhirLink2.setIdentifier(identifier2);
        fhirLink2.setResourceId(UUID.fromString("6b26fd1d-77df-4094-937a-866f66ae2a8e"));
        fhirLinkRepository.save(fhirLink2);
        user2.setFhirLinks(new HashSet<FhirLink>());
        user2.getFhirLinks().add(fhirLink2);
        userRepository.save(user2);

        // no fhirlink
        User user3 = dataTestUtils.createUser("testUser");
        user3.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier3 = new Identifier();
        identifier3.setIdentifier("test3");
        identifier3.setUser(user3);
        user3.getIdentifiers().add(identifier3);
        identifierRepository.save(identifier3);
        userRepository.save(user3);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user, group2, role);
        dataTestUtils.createGroupRole(user3, group, role);

        Long count = fhirLinkRepository.findPatientCountWithoutFhirLink(group.getId());

        assertEquals("Should be correct count", (Long) 2L, count);
    }

    @Test
    public void testFindPatientCountWithoutFhirLinkAfterDate() {
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        // no fhirlink
        User user = dataTestUtils.createUser("testUser");
        user.setIdentifiers(new HashSet<Identifier>());
        user.setCurrentLogin(new Date());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("test");
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        identifierRepository.save(identifier);
        userRepository.save(user);

        // has fhirlink
        User user2 = dataTestUtils.createUser("testuser2");
        user2.setIdentifiers(new HashSet<Identifier>());
        user2.setCurrentLogin(new Date());
        Identifier identifier2 = new Identifier();
        identifier2.setIdentifier("1");
        identifier2.setUser(user2);
        user2.getIdentifiers().add(identifier2);
        identifierRepository.save(identifier2);
        FhirLink fhirLink2 = new FhirLink();
        fhirLink2.setUser(user2);
        fhirLink2.setGroup(group2);
        fhirLink2.setIdentifier(identifier2);
        fhirLink2.setResourceId(UUID.fromString("6b26fd1d-77df-4094-937a-866f66ae2a8e"));
        fhirLinkRepository.save(fhirLink2);
        user2.setFhirLinks(new HashSet<FhirLink>());
        user2.getFhirLinks().add(fhirLink2);
        userRepository.save(user2);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user, group2, role);

        Long count = fhirLinkRepository.findPatientCountWithoutFhirLinkAndRecentLogin(group.getId(),
                new DateTime(new Date()).minusMonths(3).toDate());

        assertEquals("Should be correct count", (Long) 1L, count);
    }
}
