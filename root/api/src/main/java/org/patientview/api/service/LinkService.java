package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Link service for CRUD operations relating to Links, attached to either Groups or Codes.
 *
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface LinkService {

    /**
     * Add a Link to a Code.
     * @param codeId ID of Code to add Link to
     * @param link Link object to add to Code
     * @return Link object, newly created (note: consider just returning ID)
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Link addCodeLink(Long codeId, Link link) throws ResourceNotFoundException;

    /**
     * Add a Link to a Group.
     * @param groupId ID of Group to add Link to
     * @param link Link object to add to Group
     * @return Link object, newly created (note: consider just returning ID)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.GP_ADMIN })
    Link addGroupLink(Long groupId, Link link) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a Link.
     * @param linkId ID of Link to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long linkId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Link given an ID (Note: not currently used).
     * @param linkId ID of Link to retrieve
     * @return Link object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Link get(Long linkId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Update a Link.
     * @param link Link object to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Link save(Link link) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Need to reorder Links and set correct display order should be:
     * 1. NHS Choices Links
     * 2. MedlinePlus Links
     * 3. Any Custom Links
     *
     * @param code Code object to reorder links for
     */
    void reorderLinks(String code);
}
