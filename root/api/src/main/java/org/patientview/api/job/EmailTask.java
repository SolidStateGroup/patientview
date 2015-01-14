package org.patientview.api.job;

import org.patientview.api.service.AlertService;
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
    private AlertService alertService;

    /**
     * Check for unsent alerts, run every 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void sendAlertEmails() {
        LOG.info("Sending any new email alerts (every 10m)");
        alertService.sendAlertEmails();
    }
}
