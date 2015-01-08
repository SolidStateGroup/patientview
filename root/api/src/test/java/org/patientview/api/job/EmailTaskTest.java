package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.Timer;
import org.patientview.persistence.repository.AlertObservationHeadingRepository;

import java.util.Properties;

import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
public class EmailTaskTest {

    @Mock
    AlertObservationHeadingRepository alertObservationHeadingRepository;

    @Mock
    EmailService emailService;

    @Mock
    ObservationHeadingService observationHeadingService;

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
        emailTask.sendAlertObservationHeadingEmails();
        verify(observationHeadingService, Mockito.times(1)).sendAlertObservationHeadingEmails();
    }
}