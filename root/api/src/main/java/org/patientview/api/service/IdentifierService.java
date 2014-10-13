package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface IdentifierService extends CrudService<Identifier> {

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void saveIdentifier(Identifier identifier) throws ResourceNotFoundException, EntityExistsException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long identifierId);
}
