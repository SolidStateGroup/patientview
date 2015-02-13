package org.patientview.importer.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.AuditActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {
    
    Long getImporterUserId() throws ResourceNotFoundException;

    void createAudit(AuditActions auditActions, String identifier, String unitCode,
                     String information, String xml, Long importerUserId);
}
