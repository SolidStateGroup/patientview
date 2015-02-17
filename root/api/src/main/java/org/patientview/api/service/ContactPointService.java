package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ContactPoint service, used for CRUD operations for ContactPoints, a property of Groups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ContactPointService {

    // used by migration
    ContactPointType getContactPointType(String type) throws ResourceInvalidException;

    /**
     * Add a new ContactPoint to a Group.
     * @param groupId ID of Group to add ContactPoint to
     * @param contactPoint ContactPoint object containing all required properties
     * @return ContactPoint, newly created (consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    ContactPoint add(Long groupId, ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a ContactPoint given an ID.
     * @param contactPointId ID of ContactPoint to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long contactPointId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a ContactPoint. Note: not currently used.
     * @param contactPointId ID of ContactPoint to retrieve
     * @return ContactPoint
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    ContactPoint get(Long contactPointId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Save an updated ContactPoint.
     * @param contactPoint ContactPoint object to save
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    ContactPoint save(ContactPoint contactPoint) throws ResourceNotFoundException, ResourceForbiddenException;
}
