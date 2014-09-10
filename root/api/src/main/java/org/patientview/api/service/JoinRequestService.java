package org.patientview.api.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.JoinRequest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    JoinRequest get(Long joinRequestId) throws ResourceNotFoundException;

    BigInteger getCount(Long userId) throws ResourceNotFoundException;

    JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException;

    Page<JoinRequest> getByUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException;

    JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException ;
}
