package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ExternalServiceService;
import org.patientview.api.service.Timer;
import org.patientview.persistence.model.enums.ExternalServices;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
public class ExternalServicesTaskTest {

    @Mock
    ExternalServiceService externalServiceService;

    @Mock
    Timer timer;

    @Mock
    Properties properties;

    @InjectMocks
    ExternalServicesTask externalServicesTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRemoveOldAuditXml() throws Exception {

        when(properties.getProperty("external.service.enabled")).thenReturn("true");
        externalServicesTask.sendToExternalService();
        verify(externalServiceService, Mockito.times(1)).sendToExternalService(Collections.singletonList(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION));
    }

    @Test
    public void testRemoveOldAuditXmlDisabled() throws Exception {

        when(properties.getProperty("external.service.enabled")).thenReturn("false");
        externalServicesTask.sendToExternalService();
        verify(externalServiceService, Mockito.times(0)).sendToExternalService(Collections.singletonList(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION));
    }
}