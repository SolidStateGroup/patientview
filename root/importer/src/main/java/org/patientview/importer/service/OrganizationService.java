package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface OrganizationService {

    public UUID add(Patientview data) throws ResourceNotFoundException;

}
