package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.UserIdentifier;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Identifier service for CRUD operations and validation of User Identifiers.
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface IdentifierService {

    /**
     * Add an Identifier to a User, checking if not already in use by another User.
     * @param userId ID of User to add Identifier to
     * @param identifier String Identifier to add
     * @return Identifier object, newly created (Note: consider returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Identifier add(Long userId, Identifier identifier)
    throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    /**
     * Delete an Identifier given an ID.
     * @param identifierId ID of Identifier to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Retrieve a List of Identifier values given a Group code, used when retrieving ECS identifiers.
     * @param code String code of a Group to find User Identifier values
     * @return List of String Identifier values
     */
    List<String> findByGroupCode(String code);

    /**
     * Get an Identifier given an ID.
     * @param identifierId ID of Identifier to retrieve
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Identifier get(Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of Identifiers given a value, should only return one, used when adding patient during migration.
     * @param identifierValue
     * @return
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<Identifier> getIdentifierByValue(String identifierValue) throws ResourceNotFoundException;

    /**
     * Save an updated Identifier.
     * @param identifier Identifier to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    /**
     * Validate an Identifier, e.g. NHS Number must be within certain range, not already in use.
     * @param userIdentifier UserIdentifier object containing required information to validate Identifier value
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws EntityExistsException
     * @throws ResourceInvalidException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void validate(UserIdentifier userIdentifier)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException, ResourceInvalidException;
}
