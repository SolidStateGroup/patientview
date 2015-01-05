package org.patientview.api.job;

import org.patientview.api.service.UktService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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
     * Import UKT data, wiping out existing
     */
    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void importUktData() throws ResourceNotFoundException, FhirResourceException {
        LOG.info("Running UKT import task");
        uktService.importData();
    }
}
