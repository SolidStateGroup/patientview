package org.patientview.api.service.impl;

import org.patientview.api.service.ContactPointService;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class ContactPointServiceImpl implements ContactPointService {

    @Inject
    private ContactPointRepository contactPointRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private EntityManager entityManager;

    public ContactPoint create(final ContactPoint contactPoint) {

        if (contactPoint.getContactPointType().getId() != null) {
            contactPoint.setContactPointType(entityManager.find(ContactPointType.class, contactPoint.getContactPointType().getId()));
        }

        return contactPointRepository.save(contactPoint);
    }

    public ContactPoint getContactPoint(final Long contactPointId) {
        return contactPointRepository.findOne(contactPointId);
    }

    public void deleteContactPoint(final Long contactPointId) {
        contactPointRepository.delete(contactPointId);
    }

    public ContactPoint saveContactPoint(final ContactPoint contactPoint) {
        ContactPoint entityContactPoint = contactPointRepository.findOne(contactPoint.getId());
        entityContactPoint.setContactPointType(entityManager.find(ContactPointType.class, contactPoint.getContactPointType().getId()));
        entityContactPoint.setContent(contactPoint.getContent());
        return contactPointRepository.save(entityContactPoint);
    }
}
