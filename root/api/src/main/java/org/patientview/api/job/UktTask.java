package org.patientview.api.job;

import org.patientview.api.service.UktService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Get UKT data
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Component
public class UktTask {

    private static final Logger LOG = LoggerFactory.getLogger(UktTask.class);

    @Inject
    private UktService uktService;

    /**
     * Import UKT data, wiping out existing database contents
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void importUktData() throws ResourceNotFoundException, FhirResourceException, UktException {
        LOG.info("Running UKT import task");
        Date start = new Date();
        try {
            uktService.importData();
            LOG.info("UKT import task took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
        } catch (UktException e) {
            LOG.error("UKT exception: ", e.getMessage());
        }
    }

    /**
     * Export UKT data, wiping out existing file contents
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void exportUktData() throws ResourceNotFoundException, FhirResourceException, UktException {
        LOG.info("Running UKT export task");
        Date start = new Date();
        try {
            uktService.exportData();
            LOG.info("UKT export task took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
        } catch (UktException e) {
            LOG.error("UKT exception: ", e.getMessage());
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
