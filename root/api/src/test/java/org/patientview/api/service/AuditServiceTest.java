package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AuditServiceImpl;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
public class AuditServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @InjectMocks
    AuditService auditService = new AuditServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testFindAll() {

        Audit audit = new Audit();
        audit.setActorId(1L);

        List<Audit> audits = new ArrayList<>();
        audits.add(audit);

        when(auditRepository.findAll()).thenReturn(audits);

        List<Audit> returnedAudits = auditService.findAll();

        Assert.assertNotNull("There should be audit records", returnedAudits);
        Assert.assertEquals("There should be 1 audit record", 1, returnedAudits.size());
    }

}
