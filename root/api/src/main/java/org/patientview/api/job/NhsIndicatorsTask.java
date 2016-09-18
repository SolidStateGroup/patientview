package org.patientview.api.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.patientview.api.service.NhsIndicatorsService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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

    //@Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    @Scheduled(cron = "0 0 3 * * ?") // every day at 01:00
    public void generateNhsIndicators() {
        try {
            nhsIndicatorsService.getAllNhsIndicatorsAndStore(true);
        } catch (ResourceNotFoundException | FhirResourceException | JsonProcessingException e) {
            LOG.error("Nhs Indicators scheduled task error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
