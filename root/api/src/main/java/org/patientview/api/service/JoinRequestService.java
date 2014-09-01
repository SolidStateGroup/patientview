package org.patientview.api.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    BigInteger getCount(Long userId)
            throws ResourceNotFoundException;

    JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException;

    List<JoinRequest> get(Long userId) throws ResourceNotFoundException;

    List<JoinRequest> getByStatuses(Long userId, List<JoinRequestStatus> statuses)
            throws ResourceNotFoundException;

    JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException ;
}
