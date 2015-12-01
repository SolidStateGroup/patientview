package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.CaptchaService;
import org.patientview.api.service.RequestService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.Email;
import org.patientview.api.service.EmailService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RequestTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RequestRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class RequestServiceImpl extends AbstractServiceImpl<RequestServiceImpl> implements RequestService {

    @Inject
    private CaptchaService captchaService;

    @Inject
    private EmailService emailService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private Properties properties;

    @Inject
    private RequestRepository requestRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public Request add(Request request) throws ResourceNotFoundException, ResourceForbiddenException {

        if (captchaService.verify(request.getCaptcha())) {
            throw new ResourceForbiddenException("Captcha exception");
        }

        Group group = findGroup(request.getGroupId());

        if (group == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        request.setGroup(group);
        request.setStatus(RequestStatus.SUBMITTED);
        request.setType(request.getType());
        Request entityRequest = requestRepository.save(request);

        // attempt to find PV Admin Email address
        Email email = null;
        if (request.getType().equals(RequestTypes.JOIN_REQUEST)) {
            email = createJoinRequestEmail(entityRequest);
        } else if (request.getType().equals(RequestTypes.FORGOT_LOGIN)) {
            email = createForgottenCredentialsRequestEmail(entityRequest);
        }

        if (email != null) {
            ContactPoint contactPoint = getContactPoint(group.getContactPoints(), ContactPointTypes.PV_ADMIN_EMAIL);

            // send email, but continue if it cant be sent
            if (contactPoint == null) {
                LOG.error("No suitable group contact point set for request email");
            } else {
                email.setRecipients(new String[]{contactPoint.getContent()});
                try {
                    emailService.sendEmail(email);
                } catch (MessagingException | MailException me) {
                    LOG.error("Cannot send request email");
                }
            }
        }

        return entityRequest;
    }

    @Override
    public void completeRequests() {
        // get SUBMITTED requests
        List<RequestStatus> submittedStatus = new ArrayList<>();
        submittedStatus.add(RequestStatus.SUBMITTED);

        List<RequestTypes> requestTypes = new ArrayList<>();
        requestTypes.add(RequestTypes.JOIN_REQUEST);
        requestTypes.add(RequestTypes.FORGOT_LOGIN);

        Page<Request> requests = requestRepository.findAllByStatuses(
                submittedStatus, requestTypes, new PageRequest(0, Integer.MAX_VALUE));

        //List<Request> outDatedRequests = new ArrayList<>();

        if (requests != null) {
            for (Request request : requests.getContent()) {
                if (request.getType().equals(RequestTypes.JOIN_REQUEST)) {
                    // is a join request, so clean up identifier and search for existing user
                    if (request.getNhsNumber() != null) {
                        String identifier = request.getNhsNumber().replace(" ", "").trim();
                        if (StringUtils.isNotEmpty(identifier)) {
                            List<Identifier> identifiers = identifierRepository.findByValue(identifier);
                            if (!identifiers.isEmpty()) {
                                // user with this identifier already exists
                                // only COMPLETE if patient creation date after request date
                                // (user may have forgotten about account)
                                if (identifiers.get(0).getUser().getCreated().after(request.getCreated())) {
                                    // set to COMPLETED and save
                                    request.setStatus(RequestStatus.COMPLETED);
                                    requestRepository.save(request);
                                }
                            }
                        }
                    }
                }
            }
        }
        //Strip identifier entered in join request of all spaces
        //Search for matching identifier in patient list
        //If a match is found then set join request status to Completed UNLESS the patient date created is earlier than the join request date (implying that the patient may have forgotten about their account).
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

    private Page<org.patientview.api.model.Request> convertPageToTransport(
            Page<Request> requestPage, Pageable pageable, long total) {
        List<org.patientview.api.model.Request> requests = new ArrayList<>();

        for (Request request : requestPage.getContent()) {
            requests.add(new org.patientview.api.model.Request(request));
        }

        return new PageImpl<>(requests, pageable, total);
    }

    private List<RequestStatus> convertStringArrayToStatusList(String[] statuses) {
        List<RequestStatus> statusList = new ArrayList<>();
        for (String status : statuses) {
            RequestStatus found = RequestStatus.valueOf(status);
            if (found != null) {
                statusList.add(found);
            }
        }
        return statusList;
    }

    private Email createForgottenCredentialsRequestEmail(Request request) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Forgot Login Request");

        String body = "Dear Sir/Madam, <br/><br/> "
                + "A patient has made a forgotten login request for the <a href=\""
                + properties.getProperty("site.url")
                + "\">PatientView website</a>."
                + "<br/><br/> Please log in to PatientView to see the details of the request then follow it up "
                + "using your usual process";

        email.setBody(body);
        return email;
    }

    private Email createJoinRequestEmail(Request request) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Join Request");

        String body = "Dear Sir/Madam, <br/><br/> "
                + "A patient has made a request on the website to join <a href=\""
                + properties.getProperty("site.url")
                + "\">PatientView</a>."
                + "<br/><br/> Please log in to PatientView to see the details of the request then follow it up "
                + "using your usual process";

        email.setBody(body);
        return email;
    }

    private Page<Request> findByParentUser(User user, List<RequestStatus> statusList,
                                            List<Long> groupIds, List<RequestTypes> requestTypes, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return requestRepository.findByParentUser(user, requestTypes, pageable);
            } else {
                return requestRepository.findByParentUserAndGroups(user, groupIds, requestTypes, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return requestRepository.findByParentUserAndStatuses(user, statusList, requestTypes, pageable);
            } else {
                return requestRepository.findByParentUserAndStatusesAndGroups(
                        user, statusList, groupIds, requestTypes, pageable);
            }
        }
    }

    private Page<Request> findByUser(User user, List<RequestStatus> statusList,
                                          List<Long> groupIds, List<RequestTypes> requestTypes, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return requestRepository.findByUser(user, requestTypes, pageable);
            } else {
                return requestRepository.findByUserAndGroups(user, groupIds, requestTypes, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return requestRepository.findByUserAndStatuses(user, statusList, requestTypes, pageable);
            } else {
                return requestRepository.findByUserAndStatusesAndGroups(
                        user, statusList, groupIds, requestTypes, pageable);
            }
        }
    }

    private Page<Request> findAll(List<RequestStatus> statusList,
                                       List<Long> groupIds, List<RequestTypes> requestTypes, Pageable pageable) {
        if (statusList.isEmpty()) {
            if (groupIds.isEmpty()) {
                return requestRepository.findAll(requestTypes, pageable);
            } else {
                return requestRepository.findAllByGroups(groupIds, requestTypes, pageable);
            }
        } else {
            if (groupIds.isEmpty()) {
                return requestRepository.findAllByStatuses(statusList, requestTypes, pageable);
            } else {
                return requestRepository.findAllByStatusesAndGroups(statusList, groupIds, requestTypes, pageable);
            }
        }
    }

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("Could not find unit for Request with id %s"
                    , groupId));
        }
        return group;
    }

    private User findUser(Long userid) throws ResourceNotFoundException {
        User user = userRepository.findOne(userid);

        if (user == null) {
            throw new ResourceNotFoundException(String.format("Could not find user for Request with id %s"
                    , userid));
        }
        return user;
    }

    @Override
    public org.patientview.api.model.Request get(Long requestId) throws ResourceNotFoundException {
        Request entityRequest = requestRepository.findOne(requestId);

        if (entityRequest == null) {
            throw new ResourceNotFoundException("Request not found");
        }

        return new org.patientview.api.model.Request(entityRequest);
    }

    @Override
    public Page<org.patientview.api.model.Request> getByUser(Long userId, GetParameters getParameters)
            throws ResourceNotFoundException {
        User user = findUser(userId);

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        List<RequestStatus> statusList = new ArrayList<>();
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

        Page<Request> requestPage;

        // manage request types
        List<RequestTypes> requestTypes = new ArrayList<>();

        if (!ArrayUtils.isEmpty(getParameters.getTypes())) {
            for (String requestType : getParameters.getTypes()) {
                if (Util.isInEnum(requestType, RequestTypes.class)) {
                    requestTypes.add(RequestTypes.valueOf(requestType));
                }
            }
        }

        if (CollectionUtils.isEmpty(requestTypes)) {
            requestTypes.addAll(Arrays.asList(RequestTypes.values()));
        }

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            requestPage = findAll(statusList, groupIdList, requestTypes, pageable);
        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            requestPage = findByParentUser(user, statusList, groupIdList, requestTypes, pageable);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            requestPage = findByUser(user, statusList, groupIdList, requestTypes, pageable);
        } else {
            throw new SecurityException("Invalid role for requests");
        }

        return convertPageToTransport(requestPage, pageable, requestPage.getTotalElements());
    }

    @Override
    public BigInteger getCount(Long userId) throws ResourceNotFoundException {
        if (!userRepository.exists(userId)) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            return requestRepository.countSubmittedByParentUser(userId);
        } else if (doesContainRoles(RoleName.UNIT_ADMIN)) {
            return requestRepository.countSubmittedByUser(userId);
        } else if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            return requestRepository.countSubmitted();
        }

        throw new SecurityException("Invalid role for requests count");
    }

    /**
     * When saving a request, when the status of 'COMPLETE' is sent then the person who completed it is saved
     *
     * @param request
     * @return
     * @throws ResourceNotFoundException
     */
    @Override
    public org.patientview.api.model.Request save(Request request) throws ResourceNotFoundException {
        Request entityRequest = requestRepository.findOne(request.getId());

        if (entityRequest == null) {
            throw new ResourceNotFoundException("Request not found");
        }

        if (request.getStatus() == RequestStatus.COMPLETED) {
            User user = getUser();
            entityRequest.setCompletedBy(userRepository.findOne(user.getId()));
            entityRequest.setCompletionDate(new Date());
        }

        entityRequest.setStatus(request.getStatus());
        entityRequest.setNotes(request.getNotes());
        return new org.patientview.api.model.Request(requestRepository.save(entityRequest));
    }
}
