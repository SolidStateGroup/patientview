package org.patientview.importer.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {
    
    Long getImporterUserId() throws ResourceNotFoundException;

    // used by queue processor
    void createAudit(AuditActions auditActions, String identifier, String unitCode,
                     String information, String xml, Long importerUserId);

    // same as api, used when adding group role
    void createAudit(AuditActions auditActions, String username, User actor,
                     Long sourceObjectId, AuditObjectTypes sourceObjectType, Group group);
}
