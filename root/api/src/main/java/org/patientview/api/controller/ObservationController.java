package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.MigrationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class ObservationController extends BaseController<ObservationController> {

    @Inject
    private ObservationService observationService;

    @Inject
    private MigrationService migrationService;

    private static final String DEFAULT_SORT = "appliesDateTime";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    private static final Logger LOG = LoggerFactory.getLogger(ObservationController.class);

    @RequestMapping(value = "/user/{userId}/observations/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getObservationsByCode(@PathVariable("userId") Long userId,
            @PathVariable("code") String code) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(
            observationService.get(userId, code, DEFAULT_SORT, DEFAULT_SORT_DIRECTION, null),
            HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/observations", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<FhirObservationPage> getObservationsByCodes(
            @PathVariable("userId") Long userId,
            @RequestParam List<String> code, @RequestParam Long limit, @RequestParam Long offset,
            @RequestParam String orderDirection)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(
            observationService.getMultipleByCode(userId, code, limit, offset, orderDirection), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/observations/summary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ObservationSummary>> getObservationSummary(
            @PathVariable("userId") Long userId) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.getObservationSummary(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/observations/resultclusters", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addResultClusters(@PathVariable("userId") Long userId,
                                  @RequestBody List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException {
        observationService.addUserResultClusters(userId, userResultClusters);
    }

    // Migration Only
    @ExcludeFromApiDoc
    @RequestMapping(value = "/migrate/observations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void migrateObservations(@RequestBody MigrationUser migrationUser)
            throws ResourceNotFoundException, EntityExistsException, MigrationException {
        LOG.info(migrationUser.getPatientview1Id() + " pv1 id: received at controller");
        migrationService.migrateObservations(migrationUser);
    }

    // Migration Only
    @ExcludeFromApiDoc
    @RequestMapping(value = "/migrate/observationsfast", method = RequestMethod.GET)
    @ResponseBody
    public void migrateObservationsFast()
            throws ResourceNotFoundException, EntityExistsException, MigrationException {
        LOG.info("Started API based migration");
        migrationService.migrateObservationsFast();
    }
}
