package org.patientview.api.service.impl;

import org.patientview.api.service.ExternalServiceService;
import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.ExternalServiceTaskQueueItemRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Service for sending data from PatientView to external services via HTTP request.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@Service public class ExternalServiceServiceImpl implements ExternalServiceService {

    @Inject
    ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository;

    @Inject
    Properties properties;

    @Override
    public void addToQueue(ExternalServices externalService, String xml, User creator, Date created) {
        if (externalService.equals(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION)) {
            String url = properties.getProperty("external.service.rdc.url");
            String method = properties.getProperty("external.service.rdc.method");
            if (url != null && method != null && xml != null) {
                // store in queue, ready to be processed by cron job
                externalServiceTaskQueueItemRepository.save(
                        new ExternalServiceTaskQueueItem(url, method, xml, ExternalServiceTaskQueueStatus.PENDING,
                                creator, created));
            }
        }
    }

    @Override
    public void sendToExternalService() {
        // get unsent or failed
        List<ExternalServiceTaskQueueStatus> statuses = new ArrayList<>();
        statuses.add(ExternalServiceTaskQueueStatus.FAILED);
        statuses.add(ExternalServiceTaskQueueStatus.PENDING);

        List<ExternalServiceTaskQueueItem> externalServiceTaskQueueItems
                = externalServiceTaskQueueItemRepository.findByStatus(statuses);

        for (ExternalServiceTaskQueueItem externalServiceTaskQueueItem : externalServiceTaskQueueItems) {
            // todo: send to external service
        }
    }
}
