package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;

import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface PatientService {

    @UserOnly
    @RoleOnly(roles = { RoleName.PATIENT })
    List<org.patientview.api.model.Patient> get(Long userId, List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException;

    Patient get(UUID uuid) throws FhirResourceException;

    Patient buildPatient(User user, Identifier identifier);
}
