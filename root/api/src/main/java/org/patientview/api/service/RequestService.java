package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Request;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

/**
 * Request service, for managing Requests, where members of the public who are not currently Users in
 * PatientView can apply to join or have forgotten their login credentials.
 *
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface RequestService {

    /**
     * Create a new Request.
     * @param request Request to submit
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    Request add(Request request) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Request given ID.
     * @param requestId ID of Request to get
     * @return Request object
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.Request get(Long requestId) throws ResourceNotFoundException;

    /**
     * Get a Page of Requests available for view by a User given a User ID (staff user).
     * @param userId ID of User to retrieve Requests
     * @param getParameters GetParameters object with filters and pagination, page size, number etc
     * @return Page of Request
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Page<org.patientview.api.model.Request> getByUser(Long userId, GetParameters getParameters)
            throws ResourceNotFoundException;

    /**
     * Get a count of viewable submitted Requests given a user ID (staff user).
     * @param userId ID of User to retrieve submitted Request count
     * @return Long containing number of viewable submitted Requests
     */
    @UserOnly
    BigInteger getCount(Long userId) throws ResourceNotFoundException;

    /**
     * Save an updated Request, typically by staff members adding comments or changing the status.
     * @param request Request to update
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.Request save(Request request) throws ResourceNotFoundException;
}
