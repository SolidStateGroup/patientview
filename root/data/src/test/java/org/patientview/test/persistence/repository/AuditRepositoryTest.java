package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class AuditRepositoryTest {

    @Inject
    AuditRepository auditRepository;

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
    public void testFindAllByCountGroupAction() {
        Group group1 = dataTestUtils.createGroup("testGroup1");
        Group group2 = dataTestUtils.createGroup("testGroup2");
        Group group3 = dataTestUtils.createGroup("testGroup3");
        Date now = new Date();
        Date oneWeekAgo = new DateTime(now).minusWeeks(1).toDate();
        Date twoWeeksAgo = new DateTime(now).minusWeeks(2).toDate();
        Date oneDayAgo = new DateTime(now).minusDays(1).toDate();

        // group1 audits
        {
            // yes
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_FAIL);
            auditRepository.save(audit);
        }
        {
            // yes
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_VALIDATE_FAIL);
            auditRepository.save(audit);
        }
        {
            // no
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_SUCCESS);
            auditRepository.save(audit);
        }
        {
            // no
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_ADD);
            auditRepository.save(audit);
        }
        {
            // no
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(twoWeeksAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_SUCCESS);
            auditRepository.save(audit);
        }
        {
            // no
            Audit audit = new Audit();
            audit.setGroup(group1);
            audit.setCreationDate(twoWeeksAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_VALIDATE_FAIL);
            auditRepository.save(audit);
        }

        // group 2 audits
        {
            // yes
            Audit audit = new Audit();
            audit.setGroup(group2);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_VALIDATE_FAIL);
            auditRepository.save(audit);
        }
        {
            // no
            Audit audit = new Audit();
            audit.setGroup(group2);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_SUCCESS);
            auditRepository.save(audit);
        }

        // other group audits
        {
            Audit audit = new Audit();
            audit.setGroup(group3);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_FAIL);
            auditRepository.save(audit);
        }
        {
            Audit audit = new Audit();
            audit.setGroup(group3);
            audit.setCreationDate(oneDayAgo);
            audit.setAuditActions(AuditActions.PATIENT_DATA_FAIL);
            auditRepository.save(audit);
        }

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group1.getId());
        groupIds.add(group2.getId());
        List<AuditActions> auditActions = new ArrayList<>();
        auditActions.add(AuditActions.PATIENT_DATA_FAIL);
        auditActions.add(AuditActions.PATIENT_DATA_VALIDATE_FAIL);

        List<Object[]> audits = auditRepository.findAllByCountGroupAction(groupIds, oneWeekAgo, auditActions);

        Assert.assertEquals("Should be 2 audit returned", 2, audits.size());
        Assert.assertEquals("Should be correct group1", group1.getId(), audits.get(0)[0]);
        Assert.assertEquals("Should be count of 2", 2L, audits.get(0)[1]);
        Assert.assertEquals("Should be correct group2", group2.getId(), audits.get(1)[0]);
        Assert.assertEquals("Should be count of 1", 1L, audits.get(1)[1]);

    }

    @Test
    public void testFindAllNonPaged() {
        Audit audit = new Audit();
        audit.setActorId(1L);
        auditRepository.save(audit);

        List<Audit> audits = auditRepository.findAll();

        Assert.assertEquals("There is 1 audit returned", 1, audits.size());
    }

    @Test
    public void testFindAll() {
        User user = dataTestUtils.createUser("testUser");
        Long oneWeek = 604800000L;

        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setCreationDate(new Date(new Date().getTime() - oneWeek));

        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setCreationDate(new Date(new Date().getTime() - 3 * oneWeek));

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date(new Date().getTime() - oneWeek));

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(new Date().getTime() - 2 * oneWeek);
        Date end = new Date();

        Page<Audit> audits = auditRepository.findAll(start, end, PageRequest.of(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }

    @Test
    public void testFindAllByIdentifierFiltered() {

        User user = dataTestUtils.createUser("testUser");
        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setIdentifier("1111111111");
        identifier.setIdentifierType(lookup);
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);
        identifierRepository.save(identifier);
        userRepository.save(user);

        // source user (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setCreationDate(new Date());

        // source 1L (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(1L);
        audit2.setSourceObjectType("");
        audit2.setCreationDate(new Date());

        // source 2L (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date());

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(0);
        Date end = new Date();

        // filter by identifier
        String filterText = "%" + identifier.getIdentifier() + "%";
        Page<Audit> audits = auditRepository.findAllByIdentifierFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 1 audit returned (search by identifier)", 1, audits.getContent().size());

        // filter by username
        filterText = "%" + user.getUsername().toUpperCase() + "%";
        audits = auditRepository.findAllByIdentifierFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 1 audit returned (search by username)", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllFiltered_Source() {

        User user = dataTestUtils.createUser("testUser");

        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setCreationDate(new Date());

        Audit audit2 = new Audit();
        audit2.setSourceObjectId(1L);
        audit2.setSourceObjectType("");
        audit2.setCreationDate(new Date());

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date());

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllFiltered_Information() {
        String information = "username: 1234";

        Audit audit = new Audit();
        audit.setSourceObjectId(1L);
        audit.setSourceObjectType("");
        audit.setCreationDate(new Date());
        audit.setInformation(information);

        Audit audit2 = new Audit();
        audit2.setSourceObjectId(1L);
        audit2.setSourceObjectType("");
        audit2.setCreationDate(new Date());

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date());

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + information + "%";
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllFiltered_Username() {
        String username = "1234";

        Audit audit = new Audit();
        audit.setSourceObjectId(1L);
        audit.setSourceObjectType("");
        audit.setCreationDate(new Date());
        audit.setUsername(username);

        Audit audit2 = new Audit();
        audit2.setSourceObjectId(1L);
        audit2.setSourceObjectType("");
        audit2.setCreationDate(new Date());

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date());

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + username + "%";
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllFiltered_Actor() {
        User user = dataTestUtils.createUser("testUser");

        Audit audit = new Audit();
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());

        Audit audit2 = new Audit();
        audit2.setActorId(user.getId());
        audit2.setCreationDate(new Date());

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType("");
        audit3.setCreationDate(new Date());

        auditRepository.save(audit);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Date start = new Date(0);
        Date end = new Date();


        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }

    @Test
    public void testFindAllBySourceGroup() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

        // someone doing something to user in my group (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setActorId(user.getId());
        auditRepository.save(audit);

        // one of my users doing something somewhere else (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(1L);
        audit2.setActorId(user.getId());
        auditRepository.save(audit2);

        // another user does something to my group (yes)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(group.getId());
        audit3.setSourceObjectType(AuditObjectTypes.Group);
        audit3.setActorId(1L);
        auditRepository.save(audit3);

        // anything but has my group set (yes)
        Audit audit4 = new Audit();
        audit4.setGroup(group);
        auditRepository.save(audit4);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Page<Audit> audits = auditRepository.findAllBySourceGroup(groupIds, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 3 audit returned", 3, audits.getContent().size());
    }

    @Test
    public void testFindAllBySourceGroupFiltered1() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // user looking at user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // 1L looking at my group (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(group.getId());
        audit2.setSourceObjectType(AuditObjectTypes.Group);
        audit2.setActorId(1L);
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, filterText, groupIds,
                PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 1 audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllBySourceGroupFiltered2() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // user looking at user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // 1L looking at my group (yes)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(group.getId());
        audit2.setSourceObjectType(AuditObjectTypes.Group);
        audit2.setActorId(1L);
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L looking at user2 (yes)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setActorId(1L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // 1L looking at another group (no)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(1L);
        audit4.setSourceObjectType(AuditObjectTypes.Group);
        audit4.setActorId(1L);
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, filterText, groupIds,
                PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 3 audit returned", 3, audits.getContent().size());
    }

    @Test
    public void testFindAllBySourceGroupFiltered3() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // user looking at user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user2 looking at user2 (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setActorId(user2.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L looking at user2 (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // user2 looking at user (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(user.getId());
        audit4.setSourceObjectType(AuditObjectTypes.User);
        audit4.setActorId(user2.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        // 1L looking at 2L (no)
        Audit audit5 = new Audit();
        audit5.setSourceObjectId(2L);
        audit5.setSourceObjectType(AuditObjectTypes.User);
        audit5.setActorId(1L);
        audit5.setCreationDate(new Date());
        auditRepository.save(audit5);

        // 1L looking at user (yes)
        Audit audit6 = new Audit();
        audit6.setSourceObjectId(user.getId());
        audit6.setSourceObjectType(AuditObjectTypes.User);
        audit6.setActorId(1L);
        audit6.setCreationDate(new Date());
        auditRepository.save(audit6);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, filterText, groupIds, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 3 audit returned", 3, audits.getContent().size());
    }

    @Test
    public void testFindAllBySourceGroupFiltered_AnyFiltertext() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // user looking at user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user2 looking at user2 (yes)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setActorId(user2.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L looking at user2 (yes)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // user2 looking at user (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(user.getId());
        audit4.setSourceObjectType(AuditObjectTypes.User);
        audit4.setActorId(user2.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        // 1L looking at 2L (no)
        Audit audit5 = new Audit();
        audit5.setSourceObjectId(2L);
        audit5.setSourceObjectType(AuditObjectTypes.User);
        audit5.setActorId(1L);
        audit5.setCreationDate(new Date());
        auditRepository.save(audit5);

        // 1L looking at user (yes)
        Audit audit6 = new Audit();
        audit6.setSourceObjectId(user.getId());
        audit6.setSourceObjectType(AuditObjectTypes.User);
        audit6.setActorId(1L);
        audit6.setCreationDate(new Date());
        auditRepository.save(audit6);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Date start = new Date(0);
        Date end = new Date();

        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, "%%", groupIds, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 5 audit returned", 5, audits.getContent().size());
    }

    @Test
    public void testFindAllByAction() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setAuditActions(AuditActions.PATIENT_VIEW);
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.PATIENT_VIEW);
        actions.add(AuditActions.PASSWORD_CHANGE);

        Page<Audit> audits = auditRepository.findAllByAction(actions, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllByActionFiltered() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);

        // user PATIENT_VIEW user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.PATIENT_VIEW);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user CREATE user2 (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setAuditActions(AuditActions.PATIENT_DELETE);
        audit2.setActorId(user.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L PATIENT_VIEW user2 (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setAuditActions(AuditActions.PATIENT_VIEW);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // user2 PATIENT_VIEW user (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(user.getId());
        audit4.setSourceObjectType(AuditObjectTypes.User);
        audit4.setAuditActions(AuditActions.PATIENT_VIEW);
        audit4.setActorId(user2.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        // user2 CREATE user (no)
        Audit audit5 = new Audit();
        audit5.setSourceObjectId(user.getId());
        audit5.setSourceObjectType(AuditObjectTypes.User);
        audit5.setAuditActions(AuditActions.PATIENT_DELETE);
        audit5.setActorId(user2.getId());
        audit5.setCreationDate(new Date());
        auditRepository.save(audit5);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.PATIENT_VIEW);
        actions.add(AuditActions.PASSWORD_CHANGE);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllByActionFiltered(start, end, filterText, actions, PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }

    @Test
    public void testFindAllByGroupAndAction() {
        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

        // someone looking at a user in my group (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setAuditActions(AuditActions.PATIENT_VIEW);
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // someone looking at my group (yes)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(group.getId());
        audit2.setSourceObjectType(AuditObjectTypes.Group);
        audit2.setAuditActions(AuditActions.GROUP_ADD);
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.PATIENT_VIEW);
        actions.add(AuditActions.GROUP_ADD);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Page<Audit> audits
                = auditRepository.findAllByGroupAndAction(groupIds, actions, PageRequest.of(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }

    @Test
    public void testFindAllByGroupAndActionFiltered1() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // user PATIENT_VIEW user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.PATIENT_VIEW);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user DELETE user2 (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setAuditActions(AuditActions.PATIENT_DELETE);
        audit2.setActorId(user.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L PATIENT_VIEW user2 (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // 1L GROUP_ADD group (no)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(group.getId());
        audit4.setSourceObjectType(AuditObjectTypes.Group);
        audit4.setActorId(1L);
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.PATIENT_VIEW);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupAndActionFiltered(start, end, filterText, groupIds, actions,
                PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 1 audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllByGroupAndActionFiltered2() {
        User user = dataTestUtils.createUser("testUser");
        User user2 = dataTestUtils.createUser("test2User");
        Group group = dataTestUtils.createGroup("testGroup");
        Group group2 = dataTestUtils.createGroup("test2Group");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);

        dataTestUtils.createGroupRole(user, group, role);
        dataTestUtils.createGroupRole(user2, group, role);
        dataTestUtils.createGroupRole(user2, group2, role);

        // outside user
        User user3 = dataTestUtils.createUser("test3User");
        Group group3 = dataTestUtils.createGroup("test3Group");
        dataTestUtils.createGroupRole(user3, group3, role);

        // user PATIENT_VIEW user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.PATIENT_VIEW);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user3 GROUP_ADD group (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(group.getId());
        audit4.setSourceObjectType(AuditObjectTypes.Group);
        audit4.setAuditActions(AuditActions.GROUP_ADD);
        audit4.setActorId(user3.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.PATIENT_VIEW);
        actions.add(AuditActions.GROUP_ADD);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%%";
        Page<Audit> audits
                = auditRepository.findAllBySourceGroupAndActionFiltered(start, end, filterText, groupIds, actions,
                PageRequest.of(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }

    @Test
    public void testRemoveOldAuditXml() {
        User user = dataTestUtils.createUser("testUser");

        DateTime now = new DateTime();
        DateTime thirty = now.minusDays(30);
        DateTime ninety = now.minusDays(90);
        DateTime oneEighty = now.minusDays(180);

        Audit audit1 = new Audit();
        audit1.setSourceObjectId(user.getId());
        audit1.setSourceObjectType(AuditObjectTypes.User);
        audit1.setXml("xml");
        audit1.setCreationDate(now.toDate());

        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setXml("xml");
        audit2.setCreationDate(thirty.toDate());

        Audit audit3 = new Audit();
        audit3.setSourceObjectId(2L);
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setXml("xml");
        audit3.setCreationDate(oneEighty.toDate());

        auditRepository.save(audit1);
        auditRepository.save(audit2);
        auditRepository.save(audit3);

        Page<Audit> audits
                = auditRepository.findAll(oneEighty.toDate(), now.toDate(), PageRequest.of(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be 3 audit returned", 3, audits.getContent().size());

        int countWithXML = 0;
        for (Audit audit : audits) {
            if (StringUtils.isNotEmpty(audit.getXml())) {
                countWithXML += 1;
            }
        }

        Assert.assertEquals("Should be 3 audits with XML", 3, countWithXML);

        auditRepository.removeOldAuditXml(ninety.toDate());
        Page<Audit> audits2
                = auditRepository.findAll(oneEighty.toDate(), now.toDate(), PageRequest.of(0, Integer.MAX_VALUE));

        countWithXML = 0;
        for (Audit audit : audits2) {
            if (StringUtils.isNotEmpty(audit.getXml())) {
                countWithXML += 1;
            }
        }

        Assert.assertEquals("Should be 2 audits with XML", 2, countWithXML);
    }
}