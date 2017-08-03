package org.patientview.api.job;

import org.patientview.api.service.NhsChoicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled tasks to retrieve data from NHS Choices API, currently just conditions which are synchronised with Codes
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
@Component
public class NhsChoicesTask {

    private static final Logger LOG = LoggerFactory.getLogger(NhsChoicesTask.class);

    @Inject
    private NhsChoicesService nhsChoicesService;

    //@Scheduled(cron = "0 */3 * * * ?") // every 3 minutes
    @Scheduled(cron = "0 0 4 * * ?") // every day at 04:00
    public void updateAndSynchroniseCodes() {
        Date start = new Date();

        LOG.info("Starting update from NHS Choices and synchronise Codes task");

        try {
            // update NhschoicesConditions from NHS Choices API
            nhsChoicesService.updateConditionsFromJob();

            // synchronise updated NhschoicesConditions with Codes
            nhsChoicesService.synchroniseConditionsFromJob();

            LOG.info("Update from NHS Choices and synchronise Codes took "
                    + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
        } catch (Exception e) {
            LOG.error("Error updating from NHS Choices: " + e.getMessage(), e);
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
