package org.patientview.api.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface IdentifierService extends CrudService<Identifier> {

    public void saveIdentifier(Identifier identifier) throws ResourceNotFoundException, EntityExistsException;
}
