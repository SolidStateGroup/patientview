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
 * RESTful interface for patients to manage their membership of ECS. Used to retrieve medication information from GPs,
 * represented as patients being members of the ECS group and having the GP_MEDICATION Feature. The patient's status
 * (opted in, opted out etc) is stored as properties of their GP_MEDICATION Feature.
 * If a User is opted in to receive medications from their GP the UI will show a separate list of medication underneath
 * the medication information from their groups. Only User's who are members of Groups with the GP_MEDICATION feature
 * are given the option to opt in.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@RestController
public class GpMedicationController extends BaseController<GpMedicationController> {

    @Inject
    private GpMedicationService gpMedicationService;

    /**
     * Get a User's GP medication status, whether they are opted in/out etc.
     * @param userId ID of User to retrieve GP medication status for
     * @return GpMedicationStatus object with User's opt-in/out etc
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/gpmedicationstatus", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GpMedicationStatus> getGpMedicationStatus(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(gpMedicationService.getGpMedicationStatus(userId), HttpStatus.OK);
    }

    /**
     * Saves a User's GP medication status, including their preferences to opt in/out etc.
     * @param userId ID of User to save GP medication status
     * @param gpMedicationStatus GpMedicationStatus object with User's opt-in/out etc
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/gpmedicationstatus", method = RequestMethod.POST)
    @ResponseBody
    public void saveGpMedicationStatus(
            @PathVariable("userId") Long userId, @RequestBody GpMedicationStatus gpMedicationStatus)
            throws ResourceNotFoundException {
        gpMedicationService.saveGpMedicationStatus(userId, gpMedicationStatus);
    }

    /**
     * Get a list of all Identifiers associated with User's who have opted in to GP_MEDICATION feature and are part of
     * ECS (currently only patients). This is used by an external service which passes in a specific username/password
     * stored in a private properties file as ecs.username and ecs.password.
     * @param username ecs.username, a specific username set in a private .properties
     * @param password ecs.password, a specific password set in a private .properties
     * @return List of String Identifier values of User's opted-in to GP_MEDICATION feature and in ECS group
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
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
