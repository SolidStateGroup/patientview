package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class JoinRequestServiceImpl implements JoinRequestService {


    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private JoinRequestRepository joinRequestRepository;


    @Override
    public JoinRequest add(Long groupId, JoinRequest joinRequest) throws ResourceNotFoundException {

        Group group = findGroup(groupId);
        joinRequest.setGroup(group);
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);
        return joinRequestRepository.save(joinRequest);
    }

    @Override
    public List<JoinRequest> get(Long userId) throws ResourceNotFoundException {
        User user = findUser(userId);
        return Util.iterableToList(joinRequestRepository.findByUser(user));
    }

    @Override
    public List<JoinRequest> getByStatus(Long userId, JoinRequestStatus joinRequestStatuses)
            throws ResourceNotFoundException {
        User user = findUser(userId);
        return Util.iterableToList(joinRequestRepository.findByUserAndType(user, joinRequestStatuses));
    }

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("Could not find unit for Join Request");
        }
        return group;
    }

    private User findUser(Long userid) throws ResourceNotFoundException {
        User user = userRepository.findOne(userid);

        if (user == null) {
            throw new ResourceNotFoundException("Could not find unit for Join Request");
        }
        return user;
    }

    /**
     * When saving a join request, when the status of 'COMPLETE' is sent then the person who completed it is saved
     *
     * @param joinRequest
     * @return
     * @throws ResourceNotFoundException
     */
    public JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException {
        JoinRequest entityJoinRequest = joinRequestRepository.findOne(joinRequest.getId());

        if (entityJoinRequest == null) {
            throw new ResourceNotFoundException("Join Request not found");
        }

        if (joinRequest.getStatus() == JoinRequestStatus.COMPLETED) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entityJoinRequest.setCompletedBy(userRepository.findOne(user.getId()));
        }
        entityJoinRequest.setStatus(joinRequest.getStatus());
        entityJoinRequest.setNotes(joinRequest.getNotes());

        return joinRequestRepository.save(entityJoinRequest);
    }
}
