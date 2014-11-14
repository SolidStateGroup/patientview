package org.patientview.test.persistence.repository;

import junit.framework.Assert;
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
    UserRepository userRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
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

        Page<Audit> audits = auditRepository.findAll(start, end, new PageRequest(0, Integer.MAX_VALUE));

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
        Page<Audit> audits = auditRepository.findAllByIdentifierFiltered(start, end, filterText, new PageRequest(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 1 audit returned (search by identifier)", 1, audits.getContent().size());

        // filter by username
        filterText = "%" + user.getUsername().toUpperCase() + "%";
        audits = auditRepository.findAllByIdentifierFiltered(start, end, filterText, new PageRequest(0, Integer.MAX_VALUE));
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
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, new PageRequest(0, Integer.MAX_VALUE));
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
        Page<Audit> audits = auditRepository.findAllFiltered(start, end, filterText, new PageRequest(0, Integer.MAX_VALUE));
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

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Page<Audit> audits = auditRepository.findAllBySourceGroup(groupIds, new PageRequest(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
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
                new PageRequest(0, Integer.MAX_VALUE));
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
                new PageRequest(0, Integer.MAX_VALUE));
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
        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, filterText, groupIds, new PageRequest(0, Integer.MAX_VALUE));
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

        Page<Audit> audits = auditRepository.findAllBySourceGroupFiltered(start, end, "%%", groupIds, new PageRequest(0, Integer.MAX_VALUE));
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
        audit.setAuditActions(AuditActions.SWITCH_USER);
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);
        actions.add(AuditActions.CHANGE_PASSWORD);

        Page<Audit> audits = auditRepository.findAllByAction(actions, new PageRequest(0, Integer.MAX_VALUE));
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

        // user SWITCH_USER user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.SWITCH_USER);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user CREATE user2 (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setAuditActions(AuditActions.CREATE);
        audit2.setActorId(user.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L SWITCH_USER user2 (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setAuditActions(AuditActions.SWITCH_USER);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // user2 SWITCH_USER user (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(user.getId());
        audit4.setSourceObjectType(AuditObjectTypes.User);
        audit4.setAuditActions(AuditActions.SWITCH_USER);
        audit4.setActorId(user2.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        // user2 CREATE user (no)
        Audit audit5 = new Audit();
        audit5.setSourceObjectId(user.getId());
        audit5.setSourceObjectType(AuditObjectTypes.User);
        audit5.setAuditActions(AuditActions.CREATE);
        audit5.setActorId(user2.getId());
        audit5.setCreationDate(new Date());
        auditRepository.save(audit5);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);
        actions.add(AuditActions.CHANGE_PASSWORD);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllByActionFiltered(start, end, filterText, actions, new PageRequest(0, Integer.MAX_VALUE));
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
        audit.setAuditActions(AuditActions.SWITCH_USER);
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // someone looking at my group (yes)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(group.getId());
        audit2.setSourceObjectType(AuditObjectTypes.Group);
        audit2.setAuditActions(AuditActions.CREATE_GROUP);
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);
        actions.add(AuditActions.CREATE_GROUP);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        Page<Audit> audits
                = auditRepository.findAllByGroupAndAction(groupIds, actions, new PageRequest(0, Integer.MAX_VALUE));

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

        // user SWITCH_USER user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.SWITCH_USER);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user DELETE user2 (no)
        Audit audit2 = new Audit();
        audit2.setSourceObjectId(user2.getId());
        audit2.setSourceObjectType(AuditObjectTypes.User);
        audit2.setAuditActions(AuditActions.DELETE);
        audit2.setActorId(user.getId());
        audit2.setCreationDate(new Date());
        auditRepository.save(audit2);

        // 1L SWITCH_USER user2 (no)
        Audit audit3 = new Audit();
        audit3.setSourceObjectId(user2.getId());
        audit3.setSourceObjectType(AuditObjectTypes.User);
        audit3.setActorId(2L);
        audit3.setCreationDate(new Date());
        auditRepository.save(audit3);

        // 1L CREATE_GROUP group (no)
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
        actions.add(AuditActions.SWITCH_USER);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%" + user.getUsername().toUpperCase() + "%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupAndActionFiltered(start, end, filterText, groupIds, actions,
                new PageRequest(0, Integer.MAX_VALUE));
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

        // user SWITCH_USER user2 (yes)
        Audit audit = new Audit();
        audit.setSourceObjectId(user2.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setAuditActions(AuditActions.SWITCH_USER);
        audit.setActorId(user.getId());
        audit.setCreationDate(new Date());
        auditRepository.save(audit);

        // user3 CREATE_GROUP group (yes)
        Audit audit4 = new Audit();
        audit4.setSourceObjectId(group.getId());
        audit4.setSourceObjectType(AuditObjectTypes.Group);
        audit4.setAuditActions(AuditActions.CREATE_GROUP);
        audit4.setActorId(user3.getId());
        audit4.setCreationDate(new Date());
        auditRepository.save(audit4);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());
        groupIds.add(1L);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);
        actions.add(AuditActions.CREATE_GROUP);

        Date start = new Date(0);
        Date end = new Date();

        String filterText = "%%";
        Page<Audit> audits = auditRepository.findAllBySourceGroupAndActionFiltered(start, end, filterText, groupIds, actions,
                new PageRequest(0, Integer.MAX_VALUE));
        Assert.assertEquals("Should be 2 audit returned", 2, audits.getContent().size());
    }
}