package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AuditService;
import org.patientview.api.service.Timer;

import java.util.Properties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/04/2015
 */
public class AuditTaskTest {

    @Mock
    AuditService auditService;

    @Mock
    Timer timer;

    @Mock
    Properties properties;

    @InjectMocks
    AuditTask auditTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRemoveOldAuditXml() throws Exception {
        when(properties.getProperty("remove.old.audit.xml")).thenReturn("true");
        auditTask.removeOldAuditXml();
        verify(auditService, Mockito.times(1)).removeOldAuditXml();
    }
}