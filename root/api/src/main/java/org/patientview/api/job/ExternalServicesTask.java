package org.patientview.api.job;

import org.patientview.api.service.ExternalServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Scheduled external services sending task, used for sending data to external services based on external service task
 * queue.
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@Component
public class ExternalServicesTask {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServicesTask.class);
    @Inject
    private ExternalServiceService externalServiceService;

    @Inject
    private Properties properties;

    @Scheduled(cron = "0 */10 * * * ?") // every 10 minutes
    public void sendToExternalService() {
        String enabled = properties.getProperty("external.service.enabled");
        if (enabled != null && Boolean.parseBoolean(properties.getProperty("external.service.enabled"))) {
            try {
                LOG.info("Running sendToExternalService task");
                externalServiceService.sendToExternalService();
            } catch (Exception e) {
                LOG.error("Error running sendToExternalService task: " + e.getMessage(), e);
            }
        }
    }
}
