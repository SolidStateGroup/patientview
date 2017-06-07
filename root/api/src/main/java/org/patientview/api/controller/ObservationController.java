package org.patientview.api.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.MigrationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirObservationRange;
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
import java.util.Map;

/**
 * RESTful interface for management and retrieval of observations (test results), stored in FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class ObservationController extends BaseController<ObservationController> {

    @Inject
    private ApiObservationService apiObservationService;

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
        apiObservationService.addTestObservations(userId, groupId, fhirObservationRange);
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
        apiObservationService.addUserResultClusters(userId, userResultClusters);
    }

    /**
     * Used when Users enter their own results on the Enter Own Results page, takes a list of UserResultCluster and
     * stores in FHIR under the PATIENT_ENTERED Group.
     * @param userId ID of User to store patient entered results
     * @param resultClusterMap List of UserResultCluster objects used to represent a number of user entered results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/observations/resultclusters/custom", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addDialysisResultClusters(@PathVariable("userId") Long userId,
                                  @RequestBody Map<String, String> resultClusterMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceInvalidException {
        apiObservationService.addUserDialysisTreatmentResult(userId, resultClusterMap);
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
    @ApiOperation(value = "Get Observations of a Certain Type For a User", notes = "Given a User ID and observation "
            + "code, retrieve all observations.")
    @RequestMapping(value = "/user/{userId}/observations/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getObservationsByCode(@PathVariable("userId") Long userId,
            @PathVariable("code") String code)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(
            apiObservationService.get(userId, code, DEFAULT_SORT, DEFAULT_SORT_DIRECTION, null),
            HttpStatus.OK);
    }

    /**
     * Get a list of patient entered observations for a User of a specific Code (e.g. Creatinine, HbA1c).
     *
     * @param userId ID of User to retrieve observations for
     * @param code   Code of the observation type to retrieve
     * @return List of FhirObservation representing test results in FHIR
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @ApiOperation(value = "Get patient entered Observations of a Certain Type For a User",
            notes = "Given a User ID and observation code, retrieve patient entered observations.")
    @RequestMapping(value = "/user/{userId}/observations/{code}/patiententered", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getPatientEnteredObservationsByCode(
            @PathVariable("userId") Long userId, @PathVariable("code") String code)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(apiObservationService.getPatientEnteredByCode(userId, code), HttpStatus.OK);
    }

    /**
     * Get a list of patient entered observations for a User of a Dialysis Treatment result cluster.
     * <p>
     * We are using custom form to enter results hence using custom endpoint to return a list of results
     *
     * @param userId ID of User to retrieve observations for
     * @return List of FhirObservation representing test results in FHIR
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @ApiOperation(value = "Get patient entered Observations of a Dialysis Treatment result cluster For a User",
            notes = "Given a User ID, retrieve patient entered observations.")
    @RequestMapping(value = "/user/{userId}/observations/patiententered/home-dialysis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getPatientEnteredObservationsHomeDialysis(
            @PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(apiObservationService.getPatientEnteredDialysisTreatment(userId), HttpStatus.OK);
    }

    /**
     * Used when Users updated their own results on the Edit Own Results page, takes a UserResultCluster and
     * updates record in FHIR under the PATIENT_ENTERED Group.
     *
     * @param userId        ID of User to update patient entered results
     * @param enteredResult a patient entered result to updated
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/observations/patiententered/", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updatePatientEnteredResult(@PathVariable("userId") Long userId,
                                          @RequestParam(required = false) Long adminId,
                                    @RequestBody FhirObservation enteredResult)
            throws ResourceNotFoundException, FhirResourceException {

        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        apiObservationService.updatePatientEnteredResult(userId, adminId, enteredResult);
    }

    /**
     * Used when Users wants to delete their own results on the Edit Own Results page, takes a uuid and
     * deletes record from FHIR database.
     *
     * @param userId ID of User to delete patient entered results
     * @param uuid   a logical id of user entered results to be deleted
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/observations/{uuid}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deletePatientEnteredResult(@PathVariable("userId") Long userId,
                                           @PathVariable("uuid") String uuid,
                                           @RequestParam(required = false) Long adminId)
            throws ResourceNotFoundException, FhirResourceException {
        if (adminId == null || adminId == -1) {
            adminId = null;
        }
        apiObservationService.deletePatientEnteredResult(userId, adminId, uuid);
    }

    /**
     * Get FhirObservationPage representing multiple different observations by Code for a User.
     * @param userId ID of User to retrieve observations for
     * @param code List of Codes defining the types of observations to retrieve
     * @param limit Number of observations to retrieve
     * @param offset Offset (page) of observations to retrieve
     * @param orderDirection Ordering of observations e.g. DESC = date received descending
     * @return FhirObservationPage representing observation data for a User
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @ApiOperation(value = "Get Observations of Multiple Types For a User", notes = "Given a User ID and search "
            + "parameters, retrieve a page of observations.")
    @RequestMapping(value = "/user/{userId}/observations", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<FhirObservationPage> getObservationsByCodes(
            @PathVariable("userId") Long userId,
            @RequestParam List<String> code, @RequestParam Long limit, @RequestParam Long offset,
            @RequestParam String orderDirection)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(
            apiObservationService.getMultipleByCode(userId, code, limit, offset, orderDirection), HttpStatus.OK);
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
        return new ResponseEntity<>(apiObservationService.getObservationSummary(userId), HttpStatus.OK);
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
