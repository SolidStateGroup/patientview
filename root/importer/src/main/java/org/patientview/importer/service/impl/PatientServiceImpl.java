package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.builder.PatientBuilder;
import org.patientview.importer.exception.FhirResourceException;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private FhirResource fhirResource;

    private Lookup nhsIdentifier;

    @Override
    public void add(final Patientview patient) throws ResourceNotFoundException, FhirResourceException {
        LOG.info("Processing Patient NHS number: " + patient.getPatient().getPersonaldetails().getNhsno());
        Identifier identifier = matchPatient(patient);
        delete(identifier);

        Patient newPatientRecord = PatientBuilder.create(patient);
        UUID resourceId = create(newPatientRecord);
        addLink(identifier.getUser(), resourceId);

        userRepository.save(identifier.getUser());
    }

    private void delete(Identifier identifier) {
        FhirLink fhirLink = identifier.getFhirLink();
        if (fhirLink != null) {
            try {
                fhirResource.delete(fhirLink.getResourceId(), ResourceType.Patient);
            } catch (SQLException | FhirResourceException e) {
                LOG.error("Could delete patient resource ", e);
            }
        }
    }

    private UUID create(Patient patient) throws FhirResourceException {
        try {
            return fhirResource.createResource(patient);
        } catch (Exception e) {
            LOG.error("Could not create patient resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }

    private Identifier matchPatient(Patientview patientview) throws ResourceNotFoundException {
        nhsIdentifier = lookupRepository.findByTypeAndValue(LookupTypes.IDENTIFIER, "NHS_NUMBER");
        Identifier identifier = identifierRepository.findByTypeAndValue(patientview.getPatient().getPersonaldetails().getNhsno(), nhsIdentifier);

        if (identifier == null) {
            throw new ResourceNotFoundException("The NHS number is not linked with PatientView");
        }

        return identifier;
    }

    private void addLink(User user, UUID resourceId) {
        if (CollectionUtils.isEmpty(user.getFhirLinks())) {
            user.setFhirLinks(new HashSet<FhirLink>());
        }
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setResourceId(resourceId);
        fhirLink.setResourceType(ResourceType.Patient.name());

        user.getFhirLinks().add(fhirLink);
    }

}
