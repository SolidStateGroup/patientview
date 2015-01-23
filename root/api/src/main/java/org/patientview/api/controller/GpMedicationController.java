package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.api.service.GpMedicationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@RestController
public class GpMedicationController extends BaseController<GpMedicationController> {

    @Inject
    private GpMedicationService gpMedicationService;

    @RequestMapping(value = "/user/{userId}/gpmedicationstatus", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GpMedicationStatus> getGpMedicationStatus(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(gpMedicationService.getGpMedicationStatus(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/gpmedicationstatus", method = RequestMethod.POST)
    @ResponseBody
    public void saveGpMedicationStatus(
            @PathVariable("userId") Long userId, @RequestBody GpMedicationStatus gpMedicationStatus)
            throws ResourceNotFoundException {
        gpMedicationService.saveGpMedicationStatus(userId, gpMedicationStatus);
    }

    @ExcludeFromApiDoc
    @RequestMapping(value = "/ecs/getpatientidentifiers", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> getEceIdentifiers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(gpMedicationService.getEcsIdentifiers(username, password), HttpStatus.OK);
    }
}
