package org.patientview.api.job;

import org.patientview.api.service.ExternalServiceService;
import org.patientview.persistence.model.enums.ExternalServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static java.util.Collections.singletonList;

/**
 * Scheduled external services sending task, used for sending data to external services based on external service task
 * queue.
 * <p>
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@Component
public class ExternalServicesTask {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServicesTask.class);

    private final ExternalServiceService externalServiceService;
    private final Properties properties;

    @Autowired
    public ExternalServicesTask(ExternalServiceService externalServiceService,
                                Properties properties) {

        this.externalServiceService = externalServiceService;
        this.properties = properties;
    }

    private static <T> String flattenToString(List<T> items) {

        StringBuilder builder = new StringBuilder();

        for (T item : items) {

            builder.append(item.toString());
            builder.append(" ");
        }

        return builder.toString();
    }

    @Scheduled(cron = "0 */1 * * * ?") // every 1 minute
    public void sendToExternalService() {

        // As we processing items every minute we need to keep smaller set of records
        processQueueItems(singletonList(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION), PageRequest.of(0, 30));
    }

    @Scheduled(cron = "0 */1 * * * ?") // every 1 minute
    public void sendSurveysToExternalService() {

        // As we processing items every minute we need to keep smaller set of records
        processQueueItems(singletonList(ExternalServices.SURVEY_NOTIFICATION), PageRequest.of(0, 30));
    }

    // @Scheduled(cron = "0 */20 * * * ?") // every 20 minute
    @Scheduled(cron = "0 0 */6 * * ?") // every 6 hours
    public void sendInsRecordingsToExternalService() {
        // grab all items as we processing every 6 hours
        processQueueItems(singletonList(ExternalServices.UKRDC_INS_DIARY_NOTIFICATION),
                PageRequest.of(0, Integer.MAX_VALUE));
    }

    private void processQueueItems(List<ExternalServices> externalServices, Pageable pageable) {

        if (serviceIsDisabled()) {
            return;
        }

        String correlationId = UUID.randomUUID().toString();
        try {

            LOG.info("Starting external service sync with id: {} for: {}",
                    correlationId, flattenToString(externalServices));
            externalServiceService.sendToExternalService(externalServices, pageable);
        } catch (Exception e) {

            LOG.error("Error running sendToExternalService task {} : {}" + e.getMessage(), correlationId, e);
        }
    }

    private boolean serviceIsDisabled() {
        String enabled = properties.getProperty("external.service.enabled");

        return !Boolean.parseBoolean(enabled);
    }
}
