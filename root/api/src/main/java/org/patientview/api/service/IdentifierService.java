package org.patientview.api.service;

import org.patientview.persistence.model.Identifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface IdentifierService {

    Identifier get(Long identifierId);
    void delete(Long identifierId);
    Identifier save(Identifier identifier);
}
