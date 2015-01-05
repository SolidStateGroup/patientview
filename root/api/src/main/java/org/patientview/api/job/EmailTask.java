package org.patientview.api.job;

import org.patientview.api.service.ObservationHeadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Send bulk emails, for alerts
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Component
public class EmailTask {

    private static final Logger LOG = LoggerFactory.getLogger(EmailTask.class);

    @Inject
    private ObservationHeadingService observationHeadingService;

    /**
     * Check for unsent email alerts, run every 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void sendAlertObservationHeadingEmails() {
        LOG.info("Sending any new result Email alerts (every 10m)");
        observationHeadingService.sendAlertObservationHeadingEmails();
    }
}
