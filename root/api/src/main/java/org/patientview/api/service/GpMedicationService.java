package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;

import java.util.List;

/**
 * GP medication service, for managing a User's opt-in/out to viewing GP medications alongside medications from their
 * Groups, also used to provide list of identifiers for patients who have opted in.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public interface GpMedicationService {

    /**
     * Get list of identifiers of patients opted in to ECS given username and password of authorised user.
     * @param username authorised username
     * @param password password for username
     */
    List<String> getEcsIdentifiers(String username, String password)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Gets status of user's opt in/out to receiving medication data from their GPs (ECS/ECR).
     * @param userId user to get GP medication opt in/out status and if available
     * @return GpMedicationStatus obejct containing information on user's opt in/out status and GP meds availability
     * @throws org.patientview.config.exception.ResourceNotFoundException
     */
    @UserOnly
    GpMedicationStatus getGpMedicationStatus(Long userId) throws ResourceNotFoundException;

    /**
     * Update user's opt in/out status for medication from GP.
     * @param userId user to chagne opt in/out status
     * @param gpMedicationStatus transport object containing user's opt in/out status
     * @throws org.patientview.config.exception.ResourceNotFoundException
     */
    @UserOnly
    void saveGpMedicationStatus(Long userId, GpMedicationStatus gpMedicationStatus)
            throws ResourceNotFoundException;
}
