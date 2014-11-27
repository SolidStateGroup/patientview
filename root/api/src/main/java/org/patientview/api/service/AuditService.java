package org.patientview.api.service;

import org.patientview.api.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {

    void createAudit(AuditActions auditActions, String preValue, org.patientview.persistence.model.User actor,
                            Long sourceObjectId, AuditObjectTypes sourceObjectType, Group group);

    org.patientview.persistence.model.Audit save(org.patientview.persistence.model.Audit audit);

    Page<Audit> findAll(GetParameters getparameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    void deleteUserFromAudit(User user);
}


