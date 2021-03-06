package org.patientview.api.job;

import org.patientview.api.service.NhsIndicatorsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled tasks for generating NHS Indicators statistics.
 * Created by jamesr@solidstategroup.com
 * Created on 16/09/2016
 */
@Component
public class NhsIndicatorsTask {

    private static final Logger LOG = LoggerFactory.getLogger(NhsIndicatorsTask.class);

    @Inject
    private NhsIndicatorsService nhsIndicatorsService;

    //@Scheduled(cron = "0 */5 * * * ?") // every 5m
    @Scheduled(cron = "0 0 3 * * ?") // every day at 03:00
    public void generateNhsIndicators() {
        try {
            LOG.info("Running generate NHS indicators task (UNIT only)");
            Date start = new Date();
            nhsIndicatorsService.getAllNhsIndicatorsAndStore(true);
            LOG.info("NHS indicators task took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
        } catch (Exception e) {
            LOG.error("Nhs Indicators scheduled task error: " + e.getMessage(), e);
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
