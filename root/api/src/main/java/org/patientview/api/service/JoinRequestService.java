package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * Join request service, for managing JoinRequests, where members of the public who are not currently Users in
 * PatientView can apply to join.
 *
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface JoinRequestService {

    /**
     * Create a new JoinRequest.
     * @param joinRequest JointRequest to submit
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    JoinRequest add(JoinRequest joinRequest) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a JoinRequest given ID.
     * @param joinRequestId ID of JoinRequest to get
     * @return JoinRequest object
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.JoinRequest get(Long joinRequestId) throws ResourceNotFoundException;

    /**
     * Get a Page of JoinRequests available for view by a User given a User ID (staff user).
     * @param userId ID of User to retrieve JoinRequest
     * @param getParameters GetParameters object with filters and pagination, page size, number etc
     * @return Page of JoinRequest
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Page<org.patientview.api.model.JoinRequest> getByUser(Long userId, GetParameters getParameters)
    throws ResourceNotFoundException;

    /**
     * Get a count of viewable submitted JoinRequests given a user ID (staff user).
     * @param userId ID of User to retrieve submitted JoinRequest count
     * @return Long containing number of viewable submitted JoinRequests
     */
    @UserOnly
    BigInteger getCount(Long userId) throws ResourceNotFoundException;

    // used by migration
    void migrate(List<JoinRequest> joinRequests);

    /**
     * Save an updated JoinRequest, typically by staff members adding comments or changing the status.
     * @param joinRequest JointRequest to update
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.JoinRequest save(JoinRequest joinRequest) throws ResourceNotFoundException;
}
