package org.patientview.api.job;

import org.patientview.api.service.ReviewService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Job to pull in all facebook reviews and filter out 4 and 5* reviews
 */
@Component
public class FacebookReviewTask {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookReviewTask.class);

    @Inject
    private ReviewService reviewService;

    /**
     * Pull in new reviews
     */
    @Scheduled(cron = "0 0 2 * * ?") // every day at 02:00
    public void removeOldAuditXml() throws ResourceNotFoundException, FhirResourceException, UktException {
        try {
            LOG.info("Getting new Facebook reviews");
            reviewService.pollForNewReviews();
        } catch (Exception e) {
            LOG.error("Error getting new reviews: " + e.getMessage(), e);
        }
    }

}
