package org.patientview.api.service;

import org.patientview.api.model.Audit;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit service, used for creating, modifying, retrieving Audits, used when the security context cannot be used (e.g.
 * Logon)
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ApiAuditService {

    /**
     * Gets a Page of Audit information, with pagination parameters passed in as GetParameters.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page containing a number of Audit objects, each of which has a Date, Action etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    Page<Audit> findAll(GetParameters getParameters) throws ResourceNotFoundException, ResourceForbiddenException;
}
