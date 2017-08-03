package org.patientview.api.job;

import org.patientview.api.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Scheduled email sending task, used for result and letter alert email sending.
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Component
public class EmailTask {

    private static final Logger LOG = LoggerFactory.getLogger(EmailTask.class);

    @Inject
    private AlertService alertService;

    /**
     * Check for result and letter alerts, updated by importer, which require an email or mobile notifications
     * to be sent to Users and then email or push notification to them, run every 10 minutes.
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void sendAlertEmails() {
        //alertService.sendAlertEmails();
        LOG.info("sendAlertEmails task started");
        try {
            alertService.sendIndividualAlertEmails();
            alertService.pushNotifications();
            LOG.info("sendAlertEmails task finished");
        } catch (Exception e) {
            LOG.error("Error in sendAlertEmails task: " + e.getMessage(), e);
        }
    }
}
