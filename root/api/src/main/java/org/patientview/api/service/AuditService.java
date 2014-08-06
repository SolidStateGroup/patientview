package org.patientview.api.service;

import org.patientview.persistence.model.Audit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {

    Audit save(Audit audit);

}


