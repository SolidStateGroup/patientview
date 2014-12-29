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
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ObservationService {

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<FhirObservation> get(Long userId, String code, String orderBy, String orderDirection, Long limit)
            throws ResourceNotFoundException, FhirResourceException;

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    FhirObservationPage getMultipleByCode(Long userId, List<String> codes, Long limit,
                                          Long offset, String orderDirection)
            throws ResourceNotFoundException, FhirResourceException;

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<ObservationSummary> getObservationSummary(Long userId)
            throws ResourceNotFoundException, FhirResourceException;

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    void addUserResultClusters(Long userId, List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException;

    FhirDatabaseObservation buildFhirDatabaseObservation(
            org.patientview.persistence.model.FhirObservation fhirObservation,
            ObservationHeading observationHeading, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException;

    FhirDatabaseObservation buildFhirDatabaseNonTestObservation(
            org.patientview.persistence.model.FhirObservation fhirObservation, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException;

    List<org.patientview.api.model.FhirObservation> getByFhirLinkAndCodes(
            final FhirLink fhirLink, final List<String> codes)
            throws ResourceNotFoundException, FhirResourceException;
}
