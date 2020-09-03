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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus.FAILED;
import static org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus.PENDING;

/**
 * Service for sending data from PatientView to external services via HTTP request.
 * <p>
 * Created by jamesr@solidstategroup.com Created on 30/04/2015
 */
@Service
public class ExternalServiceServiceImpl extends AbstractServiceImpl<ExternalServiceServiceImpl>
        implements ExternalServiceService {

    private static final int HTTP_OK = 200;

    private final ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository;
    private final Properties properties;

    public ExternalServiceServiceImpl(ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository,
                                      Properties properties) {

        this.externalServiceTaskQueueItemRepository = externalServiceTaskQueueItemRepository;
        this.properties = properties;
    }

    private static org.apache.http.HttpResponse post(String content, String url) throws Exception {
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(content));
        post.setHeader("Content-type", "application/xml");
        return httpClient.execute(post);
    }

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
                        new ExternalServiceTaskQueueItem(
                                url,
                                method,
                                xml,
                                PENDING,
                                externalService,
                                creator,
                                created));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToQueue(ExternalServices externalServices, String xml, User creator,
                           Date created) {
        String url = properties.getProperty("external.service.rdc.url");
        String method = properties.getProperty("external.service.rdc.method");

        if (url != null && method != null && xml != null) {

            // store in queue, ready to be processed by cron job
            externalServiceTaskQueueItemRepository.save(
                    new ExternalServiceTaskQueueItem(
                            url,
                            method,
                            xml,
                            PENDING,
                            externalServices,
                            creator, created));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void sendToExternalService(List<ExternalServices> externalServices, Pageable pageable) {

        long start = System.currentTimeMillis();
        LOG.info("Starting external task ");
        List<ExternalServiceTaskQueueItem> externalServiceTaskQueueItems =
                getUnsentOrFailedItems(externalServices, pageable);

        List<ExternalServiceTaskQueueItem> tasksToUpdate = new ArrayList<>();
        List<ExternalServiceTaskQueueItem> tasksToDelete = new ArrayList<>();

        for (ExternalServiceTaskQueueItem queueItem : externalServiceTaskQueueItems) {

            if (isPost(queueItem)) {

                try {
                    LOG.debug("Processing Item " + queueItem.getId() + " url " + queueItem.getUrl());
                    queueItem.setStatus(ExternalServiceTaskQueueStatus.IN_PROGRESS);
                    queueItem.setLastUpdate(new Date());

                    // Update the queue status
                    externalServiceTaskQueueItemRepository.save(queueItem);

                    HttpResponse response
                            = post(queueItem.getContent(), queueItem.getUrl());

                    if (response.getStatusLine().getStatusCode() == HTTP_OK) {

                        // OK, delete queue item
                        tasksToDelete.add(queueItem);
                    } else {
                        LOG.error("ERROR Sending to external task, status " + response.getStatusLine().getStatusCode());

                        // not OK, set as failed
                        queueItem.setStatus(FAILED);
                        queueItem.setResponseCode(
                                Integer.valueOf(response.getStatusLine().getStatusCode()));
                        queueItem.setResponseReason(response.getStatusLine().getReasonPhrase());
                        queueItem.setLastUpdate(new Date());
                    }

                    LOG.debug("Done Processing Item " + queueItem.getId());
                } catch (Exception e) {
                    // exception, set as failed
                    LOG.error("Exception in Sending external task", e.getMessage());

                    queueItem.setStatus(FAILED);
                    queueItem.setLastUpdate(new Date());
                }

                tasksToUpdate.add(queueItem);
            }
        }

        //Save all in one go
        externalServiceTaskQueueItemRepository.saveAll(tasksToUpdate);

        //Delete all in one go
        externalServiceTaskQueueItemRepository.deleteAll(tasksToDelete);

        LOG.info("TIMING external task processing " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Finds queue items of a certain type with status FAILED or PENDING.
     */
    private List<ExternalServiceTaskQueueItem> getUnsentOrFailedItems(List<ExternalServices> externalService,
                                                                      Pageable pageable) {

        List<ExternalServiceTaskQueueStatus> statuses = asList(FAILED, PENDING);

        return externalServiceTaskQueueItemRepository.findByStatusAndServiceType(statuses, externalService, pageable);
    }

    /**
     * Checks if the queue item is of type POST.
     */
    private boolean isPost(ExternalServiceTaskQueueItem item) {
        return item.getMethod().equals("POST");
    }
}
