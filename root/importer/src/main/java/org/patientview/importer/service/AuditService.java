package org.patientview.importer.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {

    Audit save(Audit audit);

    Long getImporterUserId() throws ResourceNotFoundException;

    User getUserByIdentifier(String identifier);

    Group getGroupByCode(String unitCode);
}


