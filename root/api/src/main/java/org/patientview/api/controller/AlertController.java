package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.Alert;
import org.patientview.api.model.ContactAlert;
import org.patientview.api.model.ImportAlert;
import org.patientview.api.service.AlertService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.AlertTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for the modifying and retrieving Alerts.
 * Alerts are created by a user to inform them when new result data or letters comes in via the importer. A user can
 * choose to be notified in the web interface (a "New" label is shown on the dashboard) or via email. Emails do not
 * contain any real patient data for privacy reasons.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
@RestController
@ExcludeFromApiDoc
public class AlertController extends BaseController<AlertController> {

    @Inject
    private AlertService alertService;

    /**
     * Add an Alert for a User, can be a AlertTypes.RESULT or AlertTypes.LETTER alert.
     * @param userId ID of User adding Alert
     * @param alert Alert object, with User preferences, e.g. observation heading and web/email notification preferences
     * @return created Alert
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/alert", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Alert> addAlert(@PathVariable("userId") Long userId, @RequestBody Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(alertService.addAlert(userId, alert), HttpStatus.OK);
    }

    /**
     * Get a User's Alerts, given the AlertTypes type of Alert.
     * @param userId ID of User to retrieve Alerts for
     * @param alertType Type of the Alert, AlertTypes.RESULT or AlertTypes.LETTER
     * @return A List of Alert of type AlertTypes
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/alerts/{alertType}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Alert>> getAlerts(
            @PathVariable("userId") Long userId, @PathVariable("alertType") AlertTypes alertType)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(alertService.getAlerts(userId, alertType), HttpStatus.OK);
    }

    /**
     * Get alerts per group where groups are missing important contact information, used on dashboard.
     * @param userId ID of User to get contact alerts for
     * @return List of ContactAlert objects
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/contactalerts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ContactAlert>> getContactAlerts(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(alertService.getContactAlerts(userId), HttpStatus.OK);
    }

    /**
     * Get alerts per group with count of failed imports, used on dashboard.
     * @param userId ID of User to get import alerts for
     * @return List of ImportAlert objects
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/importalerts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ImportAlert>> getImportAlerts(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(alertService.getImportAlerts(userId), HttpStatus.OK);
    }

    /**
     * Remove a User's Alert.
     * @param userId ID of User to remove Alert from
     * @param alertId ID of Alert to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/alerts/{alertId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeAlert(@PathVariable("userId") Long userId, @PathVariable("alertId") Long alertId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        alertService.removeAlert(userId, alertId);
    }

    /**
     * Update a User's preferences for an alert, such as the notification settings.
     * @param userId ID of User to change the Alert preferences for
     * @param alert Alert object, containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/alert", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateAlert(@PathVariable("userId") Long userId, @RequestBody Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException {
                alertService.updateAlert(userId, alert);
    }
}
