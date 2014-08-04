package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class JoinRequestServiceImpl implements JoinRequestService {


    @Inject
    private GroupRepository groupRepository;


    @Inject
    private JoinRequestRepository joinRequestRepository;


    @Override
    public JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException {

        Group group = findGroup(groupId);
        joinRequest.setGroup(group);
        return joinRequestRepository.save(joinRequest);
    }

    @Override
    public List<JoinRequest> get(Long groupId) throws ResourceNotFoundException {
        Group group = findGroup(groupId);
        return Util.iterableToList(joinRequestRepository.findByGroup(group));
    }

    @Override
    public List<JoinRequest> getByType(Long groupId, Set<JoinRequestStatus> joinRequestStatuses)
            throws ResourceNotFoundException {
        Group group = findGroup(groupId);
        return null;
    }

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("Could not find unit for Join Request");
        }
        return group;
    }
}
