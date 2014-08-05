package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

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

        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("Could not find unit for Join Request");
        }
        joinRequest.setGroup(group);


        return joinRequestRepository.save(joinRequest);
    }

}
