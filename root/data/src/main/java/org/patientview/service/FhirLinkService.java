package org.patientview.service;

import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * FhirLink service for shared FhirLink operations, just add for now
 *
 * Created by james@solidstategroup.com
 * Created on 08/03/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface FhirLinkService {

    FhirLink createFhirLink(User user, Identifier identifier, Group group) throws FhirResourceException;
}
