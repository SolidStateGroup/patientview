package org.patientview.api.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.patientview.api.service.ExternalServiceService;
import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.ExternalServiceTaskQueueItemRepository;
import org.springframework.scheduling.annotation.Async;
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
@Service public class ExternalServiceServiceImpl extends AbstractServiceImpl<ExternalServiceServiceImpl>
        implements ExternalServiceService {

    @Inject
    private ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository;

    @Inject
    private Properties properties;

    private static final int HTTP_OK = 200;

    @Override
    public void addToQueue(ExternalServices externalService, String xml, User creator,
                           Date created, GroupRole groupRole) {
        if (externalService.equals(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION)) {
            String url = properties.getProperty("external.service.rdc.url");
            String method = properties.getProperty("external.service.rdc.method");
            if (url != null && method != null && xml != null) {
                LOG.info(String.format("Sending to external service for group role %d user %d",
                        groupRole.getId(),
                        groupRole.getUser().getId()));

                // store in queue, ready to be processed by cron job
                externalServiceTaskQueueItemRepository.save(
                        new ExternalServiceTaskQueueItem(url, method, xml, ExternalServiceTaskQueueStatus.PENDING,
                                creator, created));
            }
        }
    }

    @Override
    @Async
    public void sendToExternalService() {
        // get unsent or failed
        List<ExternalServiceTaskQueueStatus> statuses = new ArrayList<>();
        statuses.add(ExternalServiceTaskQueueStatus.FAILED);
        statuses.add(ExternalServiceTaskQueueStatus.PENDING);

        List<ExternalServiceTaskQueueItem> externalServiceTaskQueueItems
                = externalServiceTaskQueueItemRepository.findByStatus(statuses);

        for (ExternalServiceTaskQueueItem externalServiceTaskQueueItem : externalServiceTaskQueueItems) {
            if (externalServiceTaskQueueItem.getMethod().equals("POST")) {
                try {
                    externalServiceTaskQueueItem.setStatus(ExternalServiceTaskQueueStatus.IN_PROGRESS);
                    externalServiceTaskQueueItem.setLastUpdate(new Date());
                    externalServiceTaskQueueItemRepository.save(externalServiceTaskQueueItem);

                    HttpResponse response
                            = post(externalServiceTaskQueueItem.getContent(), externalServiceTaskQueueItem.getUrl());
                    if (response.getStatusLine().getStatusCode() == HTTP_OK) {
                        // OK, delete queue item
                        externalServiceTaskQueueItemRepository.delete(externalServiceTaskQueueItem);
                    } else {
                        // not OK, set as failed
                        externalServiceTaskQueueItem.setStatus(ExternalServiceTaskQueueStatus.FAILED);
                        externalServiceTaskQueueItem.setResponseCode(
                                Integer.valueOf(response.getStatusLine().getStatusCode()));
                        externalServiceTaskQueueItem.setResponseReason(response.getStatusLine().getReasonPhrase());
                        externalServiceTaskQueueItem.setLastUpdate(new Date());
                        externalServiceTaskQueueItemRepository.save(externalServiceTaskQueueItem);
                    }
                } catch (Exception e) {
                    // exception, set as failed
                    externalServiceTaskQueueItem.setStatus(ExternalServiceTaskQueueStatus.FAILED);
                    externalServiceTaskQueueItem.setLastUpdate(new Date());
                    externalServiceTaskQueueItemRepository.save(externalServiceTaskQueueItem);
                }
            }
        }
    }

    private static org.apache.http.HttpResponse post(String content, String url) throws Exception {
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(content));
        post.setHeader("Content-type", "application/xml");
        return httpClient.execute(post);
    }
}
