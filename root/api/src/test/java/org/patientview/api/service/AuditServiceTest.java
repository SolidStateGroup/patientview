package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AuditServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
public class AuditServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @Mock
    UserRepository userRepository;

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
    public void testFindAll() throws ResourceNotFoundException, ResourceForbiddenException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        Audit audit = new Audit();
        audit.setActorId(1L);

        List<Audit> audits = new ArrayList<>();
        audits.add(audit);

        PageRequest pageRequestAll = new PageRequest(0, Integer.MAX_VALUE);
        Page<Audit> auditPage = new PageImpl<>(audits, pageRequestAll, audits.size());

        when(auditRepository.findAllFiltered(any(String.class), any(Pageable.class))).thenReturn(auditPage);
        when(userRepository.findOne(any(Long.class))).thenReturn(new User());

        Page<org.patientview.api.model.Audit> returnedAudits = auditService.findAll(new GetParameters());

        Assert.assertNotNull("There should be audit records", returnedAudits);
        Assert.assertEquals("There should be 1 audit record", 1, returnedAudits.getContent().size());
    }

}
