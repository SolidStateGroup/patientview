package org.patientview.api.service.impl;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class JoinRequestServiceImpl extends AbstractServiceImpl<JoinRequestServiceImpl> implements JoinRequestService {

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
        Iterable<JoinRequest> joinRequests;

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            joinRequests = joinRequestRepository.findByParentUser(user);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            joinRequests = joinRequestRepository.findByUser(user);
        } else if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            joinRequests = joinRequestRepository.findAll();
        } else {
            throw new SecurityException("Invalid role for join requests");
        }

        return convertIterable(joinRequests);
    }

    @Override
    public BigInteger getCount(Long userId)
            throws ResourceNotFoundException {
        User user = findUser(userId);

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            return joinRequestRepository.countSubmittedByParentUser(user);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            return joinRequestRepository.countSubmittedByUser(user);
        } else if (doesContainRoles( RoleName.GLOBAL_ADMIN)) {
            return joinRequestRepository.countSubmitted();
        }

        throw new SecurityException("Invalid role for join requests count");
    }

    @Override
    public List<JoinRequest> getByStatuses(Long userId, List<JoinRequestStatus> statuses)
            throws ResourceNotFoundException{
        User user = findUser(userId);

        Iterable<JoinRequest> joinRequests;

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            joinRequests = joinRequestRepository.findByParentUserAndStatuses(user, statuses);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            joinRequests = joinRequestRepository.findByUserAndStatuses(user, statuses);
        } else if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            joinRequests = joinRequestRepository.findByStatuses(statuses);
        } else {
            throw new SecurityException("Invalid role join requests");
        }

        return convertIterable(joinRequests);
    }

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("Could not find unit for Join Request with id %s"
                    , groupId));
        }
        return group;
    }

    private User findUser(Long userid) throws ResourceNotFoundException {
        User user = userRepository.findOne(userid);

        if (user == null) {
            throw new ResourceNotFoundException(String.format("Could not find user for Join Request with id %s"
                    , userid));
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
    @Override
    public JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException {
        JoinRequest entityJoinRequest = joinRequestRepository.findOne(joinRequest.getId());

        if (entityJoinRequest == null) {
            throw new ResourceNotFoundException("Join Request not found");
        }

        if (joinRequest.getStatus() == JoinRequestStatus.COMPLETED) {
            User user = getUser();
            entityJoinRequest.setCompletedBy(userRepository.findOne(user.getId()));
            entityJoinRequest.setCompletionDate(new Date());
        }

        entityJoinRequest.setStatus(joinRequest.getStatus());
        entityJoinRequest.setNotes(joinRequest.getNotes());
        return joinRequestRepository.save(entityJoinRequest);
    }
}
