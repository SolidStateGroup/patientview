package org.patientview.api.service.impl;

import org.patientview.persistence.model.GroupRole;
import org.patientview.api.service.IdentifierService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Service
public class IdentifierServiceImpl extends AbstractServiceImpl<IdentifierServiceImpl> implements IdentifierService {

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    private static final String GENERIC_GROUP_CODE = "GENERIC";

    public Identifier get(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findOne(identifierId);

        if (identifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return identifier;
    }

    public void delete(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findOne(identifierId);

        if (identifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        identifierRepository.delete(identifierId);
    }

    public void save(Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        Identifier entityIdentifier = identifierRepository.findOne(identifier.getId());
        if (entityIdentifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        Identifier existingIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

        if (existingIdentifier != null && !existingIdentifier.equals(entityIdentifier)) {
            throw new EntityExistsException("Cannot save Identifier, another Identifier with the same "
                    + "value already exists");
        }

        if (!isMemberOfCurrentUsersGroups(entityIdentifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityIdentifier.setIdentifier(identifier.getIdentifier());
        entityIdentifier.setIdentifierType(identifier.getIdentifierType());
        identifierRepository.save(entityIdentifier);
    }

    public Identifier add(Long userId, Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // check Identifier doesn't already exist for another user
        Identifier entityIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

        if (entityIdentifier != null) {
            if (!(user.getId().equals(entityIdentifier.getUser().getId()))) {
                throw new EntityExistsException("Identifier already exists for another patient");
            }
        }

        if (!isMemberOfCurrentUsersGroups(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        identifier.setCreator(getCurrentUser());
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);
        return identifierRepository.save(identifier);
    }

    public Identifier getIdentifierByValue(String identifierValue) throws ResourceNotFoundException {
        Identifier identifier = identifierRepository.findByValue(identifierValue);
        if (identifier == null) {
            throw new ResourceNotFoundException(String.format("Could not find identifier with value %s",
                identifierValue));
        }
        return identifier;
    }

    private boolean isMemberOfCurrentUsersGroups(User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRole.getGroup().getCode().toUpperCase().equals(GENERIC_GROUP_CODE)) {
                if (isCurrentUserMemberOfGroup(groupRole.getGroup())) {
                    return true;
                }
            }
        }

        return false;
    }
}
