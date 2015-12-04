package org.patientview.api.service;

import org.hl7.fhir.instance.model.Condition;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;
import java.util.UUID;

/**
 * Condition service, to get patient Conditions from FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface ConditionService {

    /**
     * Get a list of FHIR Conditions given a UUID from FhirLink representing the patient in FHIR.
     * @param patientUuid UUID representing the patient in FHIR
     * @return List of FHIR Conditions
     * @throws FhirResourceException
     */
    List<Condition> get(UUID patientUuid) throws FhirResourceException;

    /**
     * Get staff entered conditions for a patient if present
     * @param userId Long User ID of patient to get staff entered Conditions for
     * @return List of staff entered Conditions
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    List<Condition> getStaffEntered(Long userId)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException;

    // used by migration
    void addCondition(FhirCondition fhirCondition, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException;


    /**
     * Add a condition for another user of type DIAGNOSIS_STAFF_ENTERED, when staff users enter diagnosis for patients
     * @param patientUserId User ID of user to add diagnosis for
     * @param code String code of diagnosis
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void staffAddCondition(Long patientUserId, String code)
            throws ResourceForbiddenException, ResourceNotFoundException, FhirResourceException;
}
