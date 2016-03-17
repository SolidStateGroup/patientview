package org.patientview.test.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.service.AuditService;
import org.patientview.service.impl.AuditServiceImpl;
import org.patientview.test.util.TestUtils;

import java.util.Date;
import java.util.Properties;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/03/2016
 */
public class AuditServiceTest {

    @Mock
    AuditRepository auditRepository;

    @InjectMocks
    AuditService auditService = new AuditServiceImpl();

    @Mock
    Properties properties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testRemoveOldAuditXml() {
        when(properties.getProperty("remove.old.audit.xml")).thenReturn("true");
        when(properties.getProperty("remove.old.audit.xml.days")).thenReturn("90");

        auditService.removeOldAuditXml();

        verify(auditRepository, times(1)).removeOldAuditXml(any(Date.class));
    }
}
