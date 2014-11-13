package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
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
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindAll() {

        Audit audit = new Audit();
        audit.setActorId(1L);
        auditRepository.save(audit);

        List<Audit> audits = auditRepository.findAll();

        Assert.assertEquals("There is 1 audit returned", 1, audits.size());
    }

    @Test
    public void testFindAllPaged() {

        Audit audit = new Audit();
        audit.setActorId(1L);
        auditRepository.save(audit);

        Page<Audit> audits = auditRepository.findAll(new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllByGroup() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        auditRepository.save(audit);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        Page<Audit> audits = auditRepository.findAllByGroup(groupIds, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
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
        auditRepository.save(audit);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);

        Page<Audit> audits = auditRepository.findAllByAction(actions, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }

    @Test
    public void testFindAllByGroupAndAction() {

        User user = dataTestUtils.createUser("testUser");
        Group group = dataTestUtils.createGroup("testGroup");
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.STAFF);
        dataTestUtils.createGroupRole(user, group, role);

        Audit audit = new Audit();
        audit.setSourceObjectId(user.getId());
        audit.setAuditActions(AuditActions.SWITCH_USER);
        auditRepository.save(audit);

        List<AuditActions> actions = new ArrayList<>();
        actions.add(AuditActions.SWITCH_USER);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        Page<Audit> audits
                = auditRepository.findAllByGroupAndAction(groupIds, actions, new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }
}