package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.JoinRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException;

}
