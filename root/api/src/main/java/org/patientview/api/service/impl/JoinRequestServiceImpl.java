package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
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

    private List<JoinRequestStatus> convertStringArrayToStatusList (String[] statuses) {
        List<JoinRequestStatus> statusList = new ArrayList<>();
        for (String status : statuses) {
            JoinRequestStatus found = JoinRequestStatus.valueOf(status);
            if (found != null) {
                statusList.add(found);
            }
        }
        return statusList;
    }

    @Override
    public Page<JoinRequest> getByUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException {
        User user = findUser(userId);

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        List<JoinRequestStatus> statusList = new ArrayList<>();
        if (getParameters.getStatuses() != null) {
            statusList = convertStringArrayToStatusList(getParameters.getStatuses());
        }

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            if (statusList.isEmpty()) {
                return joinRequestRepository.findByParentUser(user, pageable);
            } else {
                return joinRequestRepository.findByParentUserAndStatuses(user, statusList, pageable);
            }
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            if (statusList.isEmpty()) {
                return joinRequestRepository.findByUser(user, pageable);
            } else {
                return joinRequestRepository.findByUserAndStatuses(user, statusList, pageable);
            }
        } else if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            if (statusList.isEmpty()) {
                return joinRequestRepository.findAll(pageable);
            } else {
                return joinRequestRepository.findAllByStatuses(statusList, pageable);
            }
        } else {
            throw new SecurityException("Invalid role for join requests");
        }
    }

    @Override
    public JoinRequest get(Long joinRequestId) throws ResourceNotFoundException {
        JoinRequest entityJoinRequest = joinRequestRepository.findOne(joinRequestId);

        if (entityJoinRequest == null) {
            throw new ResourceNotFoundException("Join Request not found");
        }

        return entityJoinRequest;
    }

    @Override
    public BigInteger getCount(Long userId) throws ResourceNotFoundException {
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
