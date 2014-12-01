package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Email;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.JoinRequestService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.JoinRequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

    @Inject
    private EmailService emailService;

    @Inject
    private Properties properties;

    @Override
    public JoinRequest add(JoinRequest joinRequest) throws ResourceNotFoundException {

        Group group = findGroup(joinRequest.getGroupId());

        if (group == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        joinRequest.setGroup(group);
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);
        JoinRequest entityJoinRequest = joinRequestRepository.save(joinRequest);

        // attempt to find PV Admin Email address
        Email email = createJoinRequestEmail(entityJoinRequest);
        ContactPoint contactPoint = getContactPoint(group.getContactPoints(), ContactPointTypes.PV_ADMIN_EMAIL);

        // send email, but continue if it cant be sent
        if (contactPoint == null) {
            LOG.error("No suitable group contact point set for join request email");
        } else {
            email.setRecipients(new String[]{contactPoint.getContent()});
            try {
                emailService.sendEmail(email);
            } catch (MessagingException me) {
                LOG.error("Cannot send join request email");
            }
        }

        return entityJoinRequest;
    }

    @Override
    public void migrate(List<JoinRequest> joinRequests) {
        joinRequestRepository.save(joinRequests);
    }

    private ContactPoint getContactPoint(Collection<ContactPoint> contactPoints,
                                                ContactPointTypes contactPointTypes) {
        if (contactPoints != null && contactPointTypes != null) {
            for (ContactPoint contactPoint : contactPoints) {
                if (contactPoint.getContactPointType().getValue().equals(contactPointTypes)) {
                    return contactPoint;
                }
            }
        }
        return null;
    }

    private List<JoinRequestStatus> convertStringArrayToStatusList(String[] statuses) {
        List<JoinRequestStatus> statusList = new ArrayList<>();
        for (String status : statuses) {
            JoinRequestStatus found = JoinRequestStatus.valueOf(status);
            if (found != null) {
                statusList.add(found);
            }
        }
        return statusList;
    }

    private Email createJoinRequestEmail(JoinRequest joinRequest) {
        Email email = new Email();
        email.setSender(properties.getProperty("smtp.sender"));
        email.setSubject("PatientView - Join Request");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String date = sdf.format(joinRequest.getDateOfBirth());

        StringBuilder body = new StringBuilder();
        body.append("Dear Sir/Madam, <br/><br/>");
        body.append("A patient has made a request on the website to join <a href=\"");
        body.append(properties.getProperty("site.url"));
        body.append("\">PatientView</a> with the following details: ");
        body.append("<br/><br/>Forename: ").append(joinRequest.getForename());
        body.append("<br/>Surname: ").append(joinRequest.getSurname());
        body.append("<br/>Date of Birth: ").append(date);
        body.append("<br/>NHS Number: ").append(joinRequest.getNhsNumber());
        body.append("<br/>Email Address: ").append(joinRequest.getEmail());
        body.append("<br/>Associated Unit: ").append(joinRequest.getGroup().getName());
        body.append("<br/><br/> Please verify these details and follow up this request with the patient using your ");
        body.append("usual process to consent and add patients to PatientView.");

        email.setBody(body.toString());
        return email;
    }

    private Page<org.patientview.api.model.JoinRequest> convertPageToTransport(
            Page<JoinRequest> joinRequestPage, Pageable pageable, long total) {

        List<org.patientview.api.model.JoinRequest> joinRequests = new ArrayList<>();

        for (JoinRequest joinRequest : joinRequestPage.getContent()) {
            joinRequests.add(new org.patientview.api.model.JoinRequest(joinRequest));
        }

        return new PageImpl<>(joinRequests, pageable, total);
    }

    @Override
    public Page<org.patientview.api.model.JoinRequest> getByUser(Long userId, GetParameters getParameters)
            throws ResourceNotFoundException {

        User user = findUser(userId);

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        List<JoinRequestStatus> statusList = new ArrayList<>();
        if (getParameters.getStatuses() != null) {
            statusList = convertStringArrayToStatusList(getParameters.getStatuses());
        }

        List<Long> groupIdList = convertStringArrayToLongs(getParameters.getGroupIds());

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

        Page<JoinRequest> joinRequestPage;

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            joinRequestPage = findAll(statusList, groupIdList, pageable);
        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            joinRequestPage = findByParentUser(user, statusList, groupIdList, pageable);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            joinRequestPage = findByUser(user, statusList, groupIdList, pageable);
        } else {
            throw new SecurityException("Invalid role for join requests");
        }

        return convertPageToTransport(joinRequestPage, pageable, joinRequestPage.getTotalElements());
    }

    private Page<JoinRequest> findByParentUser(User user, List<JoinRequestStatus> statusList,
                                                List<Long> groupIds, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findByParentUser(user, pageable);
            } else {
                return joinRequestRepository.findByParentUserAndGroups(user, groupIds, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findByParentUserAndStatuses(user, statusList, pageable);
            } else {
                return joinRequestRepository.findByParentUserAndStatusesAndGroups(user, statusList, groupIds, pageable);
            }
        }
    }

    private Page<JoinRequest> findByUser(User user, List<JoinRequestStatus> statusList,
                                          List<Long> groupIds, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findByUser(user, pageable);
            } else {
                return joinRequestRepository.findByUserAndGroups(user, groupIds, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findByUserAndStatuses(user, statusList, pageable);
            } else {
                return joinRequestRepository.findByUserAndStatusesAndGroups(user, statusList, groupIds, pageable);
            }
        }
    }

    private Page<JoinRequest> findAll(List<JoinRequestStatus> statusList,
                                       List<Long> groupIds, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findAll(pageable);
            } else {
                return joinRequestRepository.findAllByGroups(groupIds, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return joinRequestRepository.findAllByStatuses(statusList, pageable);
            } else {
                return joinRequestRepository.findAllByStatusesAndGroups(statusList, groupIds, pageable);
            }
        }
    }

    @Override
    public org.patientview.api.model.JoinRequest get(Long joinRequestId) throws ResourceNotFoundException {
        JoinRequest entityJoinRequest = joinRequestRepository.findOne(joinRequestId);

        if (entityJoinRequest == null) {
            throw new ResourceNotFoundException("Join Request not found");
        }

        return new org.patientview.api.model.JoinRequest(entityJoinRequest);
    }

    @Override
    public BigInteger getCount(Long userId) throws ResourceNotFoundException {
        if (!userRepository.exists(userId)) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            return joinRequestRepository.countSubmittedByParentUser(userId);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            return joinRequestRepository.countSubmittedByUser(userId);
        } else if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
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
    public org.patientview.api.model.JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException {
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
        return new org.patientview.api.model.JoinRequest(joinRequestRepository.save(entityJoinRequest));
    }
}
