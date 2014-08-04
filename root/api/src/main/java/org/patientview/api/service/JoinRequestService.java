package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException;

    List<JoinRequest> get(Long groupId) throws ResourceNotFoundException;

    List<JoinRequest> getByType(Long groupId, Set<JoinRequestStatus> joinRequestStatuses)
            throws ResourceNotFoundException;

    JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException ;
}
