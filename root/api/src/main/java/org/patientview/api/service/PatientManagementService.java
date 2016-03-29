package org.patientview.api.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.User;
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

    PatientManagement get(Long userId, Long groupId, Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    void save(User user, Group group, Identifier identifier, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    void save(Long userId, Long groupId, Long identifierId, PatientManagement patientManagement)
    throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    void validate(PatientManagement patientManagement) throws VerificationException;
}
