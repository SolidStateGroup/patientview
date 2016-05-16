package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * PatientManagement service for validating and saving IBD patient management information
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PatientManagementService {

    /**
     * Get a PatientManagement object containing observations, diagnosis etc for use in IBD Patient Management.
     * @param userId Long ID of User (patient)
     * @param groupId Long ID of Group
     * @param identifierId Long ID of Identifier
     * @return PatientManagement containing IBD Patient Management information
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    PatientManagement get(Long userId, Long groupId, Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Import PatientManagement information, for IBD Patient Management, used by API importer.
     * @param patientManagement PatientManagement object containing observations, diagnosis etc
     * @return ServerResponse with success/error message
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importPatientManagement(PatientManagement patientManagement);

    /**
     * Save PatientManagement information, for IBD Patient Management, used when saving from UI, API importer use
     * or adding patient Users.
     * @param user User (patient) to save Patient Management information for
     * @param group Group of User (patient)
     * @param identifier Identifier of User (patient)
     * @param patientManagement PatientManagement object containing observations, diagnosis etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(User user, Group group, Identifier identifier, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Save PatientManagement information, for IBD Patient Management, used when saving from UI
     * @param userId Long ID of User (patient)
     * @param groupId Long ID of Group
     * @param identifierId Long ID of Identifier
     * @param patientManagement PatientManagement object containing observations, diagnosis etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(Long userId, Long groupId, Long identifierId, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Save Encounters (surgeries) in PatientManagement, for IBD Patient Management, used when saving from UI
     * @param userId Long ID of User (patient)
     * @param groupId Long ID of Group
     * @param identifierId Long ID of Identifier
     * @param patientManagement PatientManagement object containing Encounters
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void saveSurgeries(Long userId, Long groupId, Long identifierId, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Validate a PatientManagement object, currently only checks Condition (diagnosis) is set with a suitable code
     * and date.
     * @param patientManagement PatientManagement object to validate
     * @throws VerificationException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void validate(PatientManagement patientManagement) throws VerificationException;
}
