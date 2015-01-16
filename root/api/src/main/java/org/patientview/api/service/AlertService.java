package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.Alert;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.AlertTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AlertService {

    @UserOnly
    List<Alert> getAlerts(Long userId, AlertTypes alertType) throws ResourceNotFoundException;

    @UserOnly
    void addAlert(Long userId, Alert alert) throws ResourceNotFoundException, FhirResourceException;

    @UserOnly
    void updateAlert(Long userId, Alert alert) throws ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    void removeAlert(Long userId, Long alertId) throws ResourceNotFoundException, ResourceForbiddenException;

    void sendAlertEmails();
}
