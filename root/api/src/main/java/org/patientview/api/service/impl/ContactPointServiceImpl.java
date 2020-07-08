package org.patientview.api.service.impl;

import org.patientview.api.service.ContactPointService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class ContactPointServiceImpl extends AbstractServiceImpl<ContactPointServiceImpl>
        implements ContactPointService {

    @Inject
    private ContactPointRepository contactPointRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private EntityManager entityManager;

    public ContactPoint add(final Long groupId, final ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        contactPoint.setGroup(group);
        contactPoint.setCreator(getCurrentUser());

        if (contactPoint.getContactPointType().getId() != null) {
            contactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                    contactPoint.getContactPointType().getId()));
        }

        return contactPointRepository.save(contactPoint);
    }

    public ContactPoint get(final Long contactPointId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        ContactPoint contactPoint = contactPointRepository.findById(contactPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact point does not exist"));

        if (!isUserMemberOfGroup(getCurrentUser(), contactPoint.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return contactPoint;
    }

    public void delete(final Long contactPointId) throws ResourceNotFoundException, ResourceForbiddenException {

        ContactPoint contactPoint = contactPointRepository.findById(contactPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact point does not exist"));

        if (!isUserMemberOfGroup(getCurrentUser(), contactPoint.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        contactPoint.getGroup().getContactPoints().remove(contactPoint);
        contactPointRepository.deleteById(contactPointId);
    }

    public ContactPoint save(final ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {
        ContactPoint entityContactPoint = contactPointRepository.findById(contactPoint.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact point does not exist"));

        if (entityContactPoint == null) {
            throw new ResourceNotFoundException("Contact point does not exist");
        }

        if (!isUserMemberOfGroup(getCurrentUser(), entityContactPoint.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityContactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                contactPoint.getContactPointType().getId()));
        entityContactPoint.setContent(contactPoint.getContent());
        return contactPointRepository.save(entityContactPoint);
    }

    // Migration Only
    public ContactPointType getContactPointType(String type) throws ResourceInvalidException {

        ContactPointTypes contactPointTypes = ContactPointTypes.valueOf(type);

        if (contactPointTypes == null) {
            throw new ResourceInvalidException("The value of the lookup is invalid");
        }

        return entityManager.createQuery(
                "SELECT c FROM ContactPointType c WHERE c.value LIKE :value", ContactPointType.class)
                .setParameter("value", contactPointTypes)
                .getSingleResult();
    }
}
