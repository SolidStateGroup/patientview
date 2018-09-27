package org.patientview.api.service;

import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServices;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Service for sending data from PatientView to external services via HTTP request.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ExternalServiceService {

    void addToQueue(ExternalServices externalService, String xml, User creator, Date created, GroupRole groupRole);

    void sendToExternalService();

    void sendTaskToExternalService(ExternalServiceTaskQueueItem externalServiceTaskQueueItem);
}
