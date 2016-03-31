package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.FhirLinkService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashSet;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/03/2016
 */
@Service
@Transactional
public class FhirLinkServiceImpl extends AbstractServiceImpl<FhirLinkServiceImpl> implements FhirLinkService {

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private PatientService patientService;

    @Override
    public FhirLink createFhirLink(User user, Identifier identifier, Group group) throws FhirResourceException {
        Patient patient = patientService.buildPatient(user, identifier);
        if (patient == null) {
            throw new FhirResourceException("error building patient");
        }

        FhirDatabaseEntity fhirPatient = null;

        try {
            fhirPatient = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error creating patient");
        }

        if (fhirPatient == null) {
            throw new FhirResourceException("error creating patient, is null");
        }

        // create FhirLink to link user to FHIR Patient at group PATIENT_ENTERED
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(fhirPatient.getLogicalId());
        fhirLink.setVersionId(fhirPatient.getVersionId());
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);

        if (CollectionUtils.isEmpty(user.getFhirLinks())) {
            user.setFhirLinks(new HashSet<FhirLink>());
        }

        user.getFhirLinks().add(fhirLink);
        fhirLinkRepository.save(fhirLink);

        return fhirLink;
    }
}
