package org.patientview.api.service.impl;

import org.patientview.api.service.IdentifierService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.repository.IdentifierRepository;
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

    public Identifier get(final Long identifierId) {
        return identifierRepository.findOne(identifierId);
    }

    public void delete(final Long identifierId) {
        identifierRepository.delete(identifierId);
    }

    public Identifier save(final Identifier identifier) {
        Identifier entityIdentifier = identifierRepository.findOne(identifier.getId());
        entityIdentifier.setIdentifier(identifier.getIdentifier());
        entityIdentifier.setIdentifierType(identifier.getIdentifierType());
        return identifierRepository.save(entityIdentifier);
    }

    public Identifier add(Identifier identifier) {
        LOG.info("Not implemented");
        return null;
    }

    public void saveIdentifier(Identifier identifier) throws ResourceNotFoundException, EntityExistsException {
        Identifier entityIdentifier = identifierRepository.findOne(identifier.getId());
        if (entityIdentifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        Identifier existingIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

        if (!existingIdentifier.equals(entityIdentifier)) {
            throw new EntityExistsException("Cannot save Identifier, another Identifier with the same "
                    + "value already exists");
        }

        entityIdentifier.setIdentifier(identifier.getIdentifier());
        entityIdentifier.setIdentifierType(identifier.getIdentifierType());
        identifierRepository.save(entityIdentifier);
    }
}
