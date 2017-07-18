package org.patientview.api.job;

import org.patientview.api.service.AlertService;
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

    @Inject
    private AlertService alertService;

    /**
     * Check for result and letter alerts, updated by importer, which require an email to be sent to Users and then
     * email them, run every 10 minutes.
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void sendAlertEmails() {
        //alertService.sendAlertEmails();
        alertService.sendIndividualAlertEmails();
        alertService.sendPushNotification();
    }
}
