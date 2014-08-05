package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException;
<<<<<<< HEAD
=======

    List<JoinRequest> get(Long groupId) throws ResourceNotFoundException;

    List<JoinRequest> getByStatus(Long userId, JoinRequestStatus joinRequestStatuses)
            throws ResourceNotFoundException;
>>>>>>> 73e9ade59079103c7eef7e11d644aedf97818ec5

    JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException ;
}
