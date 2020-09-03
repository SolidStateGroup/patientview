package org.patientview.api.service;

import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServices;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service for sending data from PatientView to external services via HTTP request.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ExternalServiceService {

    void addToQueue(ExternalServices externalService, String xml, User creator, Date created, GroupRole groupRole);

    /**
     * Add an xml payload to the queue.
     *
     * @param externalServices Type of queue message
     * @param xml              Payload to queue
     * @param creator          User
     * @param created          Date created
     */
    void addToQueue(ExternalServices externalServices, String xml, User creator, Date created);

    /**
     * Queues up items to send to external service.
     *
     * @param externalServices item types to send.
     */
    void sendToExternalService(List<ExternalServices> externalServices, Pageable pageable);
}
