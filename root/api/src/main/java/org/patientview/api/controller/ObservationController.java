package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.FhirObservationRange;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
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
 * RESTful interface for management and retrieval of observations (test results), stored in FHIR.
 *
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

    // API
    /**
     * Given a User ID, Group ID, Observation code, date range and a list of Observations, remove existing Observations
     * within the date range then store the new Observations. This follows the method when using PatientView XML.
     * @param userId ID of User to add Observations for
     * @param groupId ID of the Group associated with these Observations
     * @param fhirObservationRange FhirObservationRange object containing code, date range and List of FhirObservation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/group/{groupId}/observations", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addObservations(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
                                  @RequestBody FhirObservationRange fhirObservationRange)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        observationService.addTestObservations(userId, groupId, fhirObservationRange);
    }

    /**
     * Used when Users enter their own results on the Enter Own Results page, takes a list of UserResultCluster and
     * stores in FHIR under the PATIENT_ENTERED Group.
     * @param userId ID of User to store patient entered results
     * @param userResultClusters List of UserResultCluster objects used to represent a number of user entered results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/observations/resultclusters", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addResultClusters(@PathVariable("userId") Long userId,
                                  @RequestBody List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException {
        observationService.addUserResultClusters(userId, userResultClusters);
    }

    // API
    /**
     * Get a list of all observations for a User of a specific Code (e.g. Creatinine, HbA1c).
     * @param userId ID of User to retrieve observations for
     * @param code Code of the observation type to retrieve
     * @return List of FhirObservation representing test results in FHIR
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/observations/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getObservationsByCode(@PathVariable("userId") Long userId,
            @PathVariable("code") String code)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(
            observationService.get(userId, code, DEFAULT_SORT, DEFAULT_SORT_DIRECTION, null),
            HttpStatus.OK);
    }

    /**
     * Get FhirObservationPage representing multiple different observations by Code for a User.
     * @param userId ID of User to retrieve observations for
     * @param code List of Codes defining the types of observations to retrieve
     * @param limit Number of observations to retrieve
     * @param offset Offset (page) of observations to retrieve
     * @param orderDirection Ordering of observations e.g. date received descending
     * @return FhirObservationPage representing observation data for a User
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
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

    /**
     * Get a summary of observation data for a User, used on the default Results page.
     * @param userId ID of User to retrieve observation summary for
     * @return List of ObservationSummary representing panels of result summary information by Group (specialty)
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/observations/summary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ObservationSummary>> getObservationSummary(
            @PathVariable("userId") Long userId) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.getObservationSummary(userId), HttpStatus.OK);
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
