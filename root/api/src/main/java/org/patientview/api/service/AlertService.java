package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.Alert;
import org.patientview.api.model.ContactAlert;
import org.patientview.api.model.ImportAlert;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alert service, used for creating, modifying, retrieving Alerts and sending Alert emails.
 * Alerts are created by a user to inform them when new result data or letters comes in via the importer. A user can
 * choose to be notified in the web interface (a "New" label is shown on the dashboard) or via email. Emails do not
 * contain any real patient data for privacy reasons.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AlertService {

    /**
     * Add an Alert for a User, can be a AlertTypes.RESULT or AlertTypes.LETTER alert.
     * @param userId ID of User adding Alert
     * @param alert Alert object, with User preferences, e.g. observation heading and web/email notification preferences
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    void addAlert(Long userId, Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Get a User's Alerts, given the AlertTypes type of Alert.
     * @param userId ID of User to retrieve Alerts for
     * @param alertType Type of the Alert, AlertTypes.RESULT or AlertTypes.LETTER
     * @return A List of Alert of type AlertTypes
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<Alert> getAlerts(Long userId, AlertTypes alertType) throws ResourceNotFoundException;

    /**
     * Get alerts used for notifying if missing Users with DEFAULT_MESSAGING_CONTACT etc for a Group.
     * @param userId ID of User to get ContactAlerts for
     * @return List of ContactAlerts
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<ContactAlert> getContactAlerts(Long userId) throws ResourceNotFoundException;

    /**
     * Get alerts per group with count of failed imports, used on dashboard.
     * @param userId ID of User to get import alerts for
     * @return List of ImportAlert objects
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<ImportAlert> getImportAlerts(Long userId) throws ResourceNotFoundException;

    /**
     * Remove a User's Alert.
     * @param userId ID of User to remove Alert from
     * @param alertId ID of Alert to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void removeAlert(Long userId, Long alertId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Check for result and letter alerts, updated by importer, which require an email to be sent to Users and then
     * email them, run every 10 minutes.
     */
    void sendAlertEmails();

    void sendIndividualAlertEmails();

    /**
     * Check for results mobile alerts and sends push notifications to Users
     */
    void sendPushNotification();

    /**
     * Update a User's preferences for an alert, such as the notification settings.
     * @param userId ID of User to change the Alert preferences for
     * @param alert Alert object, containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void updateAlert(Long userId, Alert alert) throws ResourceNotFoundException, ResourceForbiddenException;
}
