package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AlertService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.Timer;

import java.util.Properties;

import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
public class EmailTaskTest {

    @Mock
    EmailService emailService;

    @Mock
    AlertService alertService;

    @Mock
    Timer timer;

    @Mock
    Properties properties;

    @InjectMocks
    EmailTask emailTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSendAlertObservationHeadingEmails() throws Exception {
        emailTask.sendAlertEmails();
        //verify(alertService, Mockito.times(1)).sendAlertEmails();
        verify(alertService, Mockito.times(1)).sendIndividualAlertEmails();
        verify(alertService, Mockito.times(1)).sendPushNotification();
    }
}