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
    public JoinRequest addJoinRequest(final JoinRequest joinRequest) throws ResourceNotFoundException {

        if (joinRequest.getSpecialty() != null) {
            Group group = groupRepository.findOne(joinRequest.getSpecialty().getId());
            if (group == null) {
                throw new ResourceNotFoundException("Could not find specialty for Join Request");
            }
            joinRequest.setSpecialty(group);
        }

        if (joinRequest.getUnit() != null) {
            Group group = groupRepository.findOne(joinRequest.getUnit().getId());
            if (group == null) {
                throw new ResourceNotFoundException("Could not find unit for Join Request");
            }
            joinRequest.setUnit(group);
        }

        return joinRequestRepository.save(joinRequest);
    }

}
