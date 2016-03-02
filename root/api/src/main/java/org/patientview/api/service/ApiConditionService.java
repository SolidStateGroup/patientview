package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;

/**
 * Condition service, to get patient Conditions from FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface ApiConditionService {
    /**
     * Get staff entered Conditions for a patient if present
     * @param userId Long User ID of patient to get staff entered Conditions for
     * @return List of staff entered Conditions
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    List<FhirCondition> getStaffEntered(Long userId)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException;

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

    /**
     * Set the status of all a User's staff entered Conditions to "refuted", equivalent to deleting
     * @param patientUserId User ID of user to set staff entered Conditions status to "refuted"
     * @throws Exception
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void staffRemoveCondition(Long patientUserId) throws Exception;
}
