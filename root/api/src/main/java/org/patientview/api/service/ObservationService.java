package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;

/**
 * Observation service, for management and retrieval of observations (test results), stored in FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ObservationService {

    /**
     * Used when Users enter their own results on the Enter Own Results page, takes a list of UserResultCluster and
     * stores in FHIR under the PATIENT_ENTERED Group.
     * @param userId ID of User to store patient entered results
     * @param userResultClusters List of UserResultCluster objects used to represent a number of user entered results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void addUserResultClusters(Long userId, List<UserResultCluster> userResultClusters)
    throws ResourceNotFoundException, FhirResourceException;

    // used by migration
    FhirDatabaseObservation buildFhirDatabaseNonTestObservation(
            org.patientview.persistence.model.FhirObservation fhirObservation, FhirLink fhirLink)
    throws ResourceNotFoundException, FhirResourceException;

    // used by migration
    FhirDatabaseObservation buildFhirDatabaseObservation(
            org.patientview.persistence.model.FhirObservation fhirObservation,
            ObservationHeading observationHeading, FhirLink fhirLink)
    throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get a list of all observations for a User of a specific Code (e.g. Creatinine, HbA1c), used in results table
     * view.
     * @param userId ID of User to retrieve observations for
     * @param code Code of the observation type to retrieve
     * @return List of FhirObservation representing test results in FHIR
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<FhirObservation> get(Long userId, String code, String orderBy, String orderDirection, Long limit)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Used when retrieving non test FHIR Observations from FHIR.
     * @param fhirLink FhirLink link between User and Patient in FHIR
     * @param codes List of Codes defining the types of observations to retrieve
     * @return List of FhirObservation used when building a patient record
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    List<org.patientview.api.model.FhirObservation> getByFhirLinkAndCodes(
            final FhirLink fhirLink, final List<String> codes)
    throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get FhirObservationPage representing multiple different observations by Code for a User.
     * @param userId ID of User to retrieve observations for
     * @param codes List of Codes defining the types of observations to retrieve
     * @param limit Number of observations to retrieve
     * @param offset Offset (page) of observations to retrieve
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
     * Get a summary of observation data for a User, used on the default Results page.
     * @param userId ID of User to retrieve observation summary for
     * @return List of ObservationSummary representing panels of result summary information by Group (specialty)
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<ObservationSummary> getObservationSummary(Long userId)
    throws ResourceNotFoundException, FhirResourceException;
}
