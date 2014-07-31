package org.patientview.api.service;

import org.patientview.api.exception.ResourceInvalidException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ContactPointService {

    ContactPoint create(ContactPoint contactPoint);

    ContactPoint getContactPoint(Long contactPointId);

    void deleteContactPoint(Long contactPointId);

    ContactPoint saveContactPoint(ContactPoint contactPoint);

    ContactPointType getContactPointType(String type) throws ResourceInvalidException;
}
