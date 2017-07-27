package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.ObservationHeading;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;
import java.util.Map;

/**
 * Observation service, for management and retrieval of observations (test results), stored in FHIR.
 * <p/>
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ApiObservationService {

    /**
     * Given a User ID, Group ID, Observation code, date range and a list of Observations, remove existing Observations
     * within the date range then store the new Observations. This follows the method when using PatientView XML.
     *
     * @param userId               ID of User to add Observations for
     * @param groupId              ID of the Group associated with these Observations
     * @param fhirObservationRange FhirObservationRange object containing code, date range and List of FhirObservation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.UNIT_ADMIN_API, RoleName.IMPORTER })
    void addTestObservations(Long userId, Long groupId, FhirObservationRange fhirObservationRange)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Used when Users enter their own results on the Enter Own Results page, takes a list of UserResultCluster and
     * stores in FHIR under the PATIENT_ENTERED Group.
     *
     * @param userId             ID of User to store patient entered results
     * @param userResultClusters List of UserResultCluster objects used to represent a number of user entered results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void addUserResultClusters(Long userId, List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Custom implementation to handle entering own results for Dialysis Treatment results.
     * Used when Users enter Dialysis Treatment results on the Enter Own Results page and
     * stores in FHIR under the PATIENT_ENTERED Group.
     *
     * @param userId             ID of User to store patient entered results
     * @param resultClusterMap List of UserResultCluster objects used to represent a number of user entered results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws ResourceInvalidException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void addUserDialysisTreatmentResult(Long userId, Map<String, String> resultClusterMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceInvalidException;


    /**
     * Used when Users update their own results on the Edit Own Results page, takes a  UserResultCluster and
     * updates record in FHIR under the PATIENT_ENTERED Group.
     *
     * @param adminId        ID of admin User(viewing patient) or patient User
     * @param userId     ID of patient User to update patient entered results
     * @param enteredResult a patient entered result to updated
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void updatePatientEnteredResult(Long userId, Long adminId, FhirObservation enteredResult)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Used when Users wants to delete their own results on the Edit Own Results page, takes a uuid and
     * deletes record from FHIR database.
     *
     * @param adminId    ID of admin User(viewing patient) or patient User
     * @param userId ID of patient User user to delete patient entered results
     * @param uuid      a logical id of user entered results to be deleted
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void deletePatientEnteredResult(Long userId, Long adminId, String uuid)
            throws ResourceNotFoundException, FhirResourceException;

    // API
    /**
     * Get a list of all observations for a User of a specific Code (e.g. Creatinine, HbA1c), used in results table
     * view.
     *
     * @param userId ID of User to retrieve observations for
     * @param code   Code of the observation type to retrieve
     * @return List of FhirObservation representing test results in FHIR
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.PATIENT, RoleName.UNIT_ADMIN_API })
    List<FhirObservation> get(Long userId, String code, String orderBy, String orderDirection, Long limit)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Get a list of patient entered observations for a User of a specific Code (e.g. Creatinine, HbA1c),
     * used in results table view when editing results.
     *
     * @param userId ID of User to retrieve observations for
     * @param code Code of the observation type to retrieve
     * @return List of FhirObservation representing patient entered test results in FHIR
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.PATIENT, RoleName.UNIT_ADMIN_API })
    List<FhirObservation> getPatientEnteredByCode(Long userId, String code)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Get a list of patient entered observations for a User of a Dialysis Treatment result cluster,
     * used in results table view when viewing Dialysis results.
     *
     * @param userId ID of User to retrieve observations for
     * @return List of FhirObservation representing patient entered test results in FHIR
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = {RoleName.PATIENT, RoleName.UNIT_ADMIN_API})
    List<FhirObservation> getPatientEnteredDialysisTreatment(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Used when retrieving non test FHIR Observations from FHIR.
     *
     * @param fhirLink FhirLink link between User and Patient in FHIR
     * @param codes    List of Codes defining the types of observations to retrieve
     * @return List of FhirObservation used when building a patient record
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    List<org.patientview.api.model.FhirObservation> getByFhirLinkAndCodes(
            final FhirLink fhirLink, final List<String> codes)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get FhirObservationPage representing multiple different observations by Code for a User.
     *
     * @param userId         ID of User to retrieve observations for
     * @param codes          List of Codes defining the types of observations to retrieve
     * @param limit          Number of observations to retrieve
     * @param offset         Offset (page) of observations to retrieve
     * @param orderDirection Ordering of observations e.g. date received descending
     * @return FhirObservationPage representing observation data for a User
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    FhirObservationPage getMultipleByCode(Long userId, List<String> codes, Long limit,
                                          Long offset, String orderDirection)
            throws ResourceNotFoundException, FhirResourceException;


    /**
     * Get FhirObservationPage representing multiple different observations by Code for a User.
     *
     * @param userId         ID of User to retrieve observations for
     * @param codes          List of Codes defining the types of observations to retrieve
     * @param orderDirection Ordering of observations e.g. date received descending
     * @param fromDate       yyyy-mm-dd date to search from
     * @param toDate         yyyy-mm-dd date to search to
     * @return FhirObservation representing observation data for a User
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    Map<Long, Map<String, List<FhirObservation>>> getObservationsByMultipleCodeAndDate(Long userId, List<String> codes,
        String orderDirection, String fromDate, String toDate) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get a summary of observation data for a User, used on the default Results page.
     *
     * @param userId ID of User to retrieve observation summary for
     * @return List of ObservationSummary representing panels of result summary information by Group (specialty)
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<ObservationSummary> getObservationSummary(Long userId) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Given a FhirObservationRange object with a start, end date and list of observations, store in FHIR
     * @param fhirObservationRange FhirObservationRange containing start date, end date, code and observations to import
     * @return ServerResponse object containing success, error message and successful status
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importObservations(FhirObservationRange fhirObservationRange);

    /**
     * Get a list of patient entered observations by given a patient user identifier (NHS number)
     * with a start and end date
     *
     * @param identifier an NHS number of the patient user to retrieve observations for
     * @param fromDate start date to search from in yyyy-mm-dd
     * @param toDate end date to search to in yyyy-mm-dd
     * @return a list of ObservationHeading objects containing user entered observations
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    List<ObservationHeading> getPatientEnteredObservations(String identifier, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException,
            ResourceForbiddenException, ResourceInvalidException;
}
