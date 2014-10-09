package org.patientview.api.service.impl;

import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.api.service.ContactPointService;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.repository.ContactPointRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

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
    private EntityManager entityManager;

    public ContactPoint add(final ContactPoint contactPoint) {

        if (contactPoint.getContactPointType().getId() != null) {
            contactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                    contactPoint.getContactPointType().getId()));
        }

        return contactPointRepository.save(contactPoint);
    }

    public ContactPoint get(final Long contactPointId) {
        return contactPointRepository.findOne(contactPointId);
    }

    public void delete(final Long contactPointId) {
        ContactPoint contactPoint = contactPointRepository.findOne(contactPointId);
        contactPoint.getGroup().getContactPoints().remove(contactPoint);
        contactPointRepository.delete(contactPointId);
    }

    public ContactPoint save(final ContactPoint contactPoint) {
        ContactPoint entityContactPoint = contactPointRepository.findOne(contactPoint.getId());
        entityContactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                contactPoint.getContactPointType().getId()));
        entityContactPoint.setContent(contactPoint.getContent());
        return contactPointRepository.save(entityContactPoint);
    }

    // Migration Only
    public ContactPointType getContactPointType(String type) throws ResourceInvalidException {

        ContactPointTypes contactPointTypes = ContactPointTypes.valueOf(type);

        if (contactPointTypes == null) {
            throw new ResourceInvalidException("The value to the lookup is invalid");
        }

        return entityManager.createQuery(
                "SELECT c FROM ContactPointType c WHERE c.value LIKE :value", ContactPointType.class)
                .setParameter("value", contactPointTypes)
                .getSingleResult();

    }
}
