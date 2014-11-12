package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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
    public void testFindAllFiltered() {

        Audit audit = new Audit();
        audit.setActorId(1L);
        auditRepository.save(audit);
        Group group = dataTestUtils.createGroup("testGroup");
        Long[] groupIdsArr = {group.getId()};

        Page<Audit> audits = auditRepository.findAllFiltered(new PageRequest(0, Integer.MAX_VALUE));

        Assert.assertEquals("Should be one audit returned", 1, audits.getContent().size());
    }
}