package org.patientview.importer.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.RoleType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupRoleService {

    void add(Long userId, Long groupId, RoleType roleType) throws ResourceNotFoundException;
}
