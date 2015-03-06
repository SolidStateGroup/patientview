package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Location service, for managing Locations attached to Groups.
 *
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface LocationService {

    /**
     * Add a Location to a Group.
     * @param groupId ID of Group to add Location to
     * @param location Location to add
     * @return Location object, newly created (note: consider just returning ID)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Location add(Long groupId, Location location)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a Location.
     * @param locationId ID of Location to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long locationId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Location given ID (Note: not currently used).
     * @param locationId ID of Location to retrieve
     * @return Location object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Location get(Long locationId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Update a Location.
     * @param location Location object to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Location save(Location location) throws ResourceNotFoundException, ResourceForbiddenException;
}
