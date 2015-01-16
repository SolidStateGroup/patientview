package org.patientview.api.controller;

import org.patientview.api.model.Alert;
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
 * Restful interface for the basic Crud operation for alerts.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
@RestController
public class AlertController extends BaseController<AlertController> {

    @Inject
    private AlertService alertService;

    // get alerts
    @RequestMapping(value = "/user/{userId}/alerts/{alertType}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Alert>> getAlerts(
            @PathVariable("userId") Long userId, @PathVariable("alertType") AlertTypes alertType)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(alertService.getAlerts(userId, alertType), HttpStatus.OK);
    }

    // add alert for result type for user
    @RequestMapping(value = "/user/{userId}/alert", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addAlert(
            @PathVariable("userId") Long userId, @RequestBody Alert alert)
            throws ResourceNotFoundException, FhirResourceException {
                alertService.addAlert(userId, alert);
    }

    // update alert for user
    @RequestMapping(value = "/user/{userId}/alert", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateAlert(@PathVariable("userId") Long userId, @RequestBody Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException {
                alertService.updateAlert(userId, alert);
    }

    // remove alert for user
    @RequestMapping(value = "/user/{userId}/alerts/{alertId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeAlert(@PathVariable("userId") Long userId, @PathVariable("alertId") Long alertId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        alertService.removeAlert(userId, alertId);
    }
}
