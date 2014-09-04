package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.builder.PatientBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.util.Util;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
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
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    private Lookup nhsIdentifier;

    /**
     * We link fhir records against NHS Number, Unit and User.
     *
     * @param patient
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @Override
    public UUID add(final Patientview patient) throws ResourceNotFoundException, FhirResourceException {
        // Find the identifier which the patient is linked to.
        Identifier identifier = matchPatient(patient);

        // Find the group that is importing the data
        Group group = groupRepository.findByCode(patient.getCentredetails().getCentrecode());

        // Find and update the link between the existing User and Unit to the Fhir Record
        FhirLink fhirLink = retrieveLink(group, identifier);
        update(fhirLink);

        // Create a new Fhir record and add the link to the User and Unit
        PatientBuilder patientBuilder = new PatientBuilder(patient);
        JSONObject jsonObject = create(patientBuilder.build());
        addLink(identifier, group, jsonObject);

        LOG.info("Processed Patient NHS number: " + patient.getPatient().getPersonaldetails().getNhsno());

        return Util.getVersionId(jsonObject);
    }

    private void update(FhirLink fhirLink) {
        if (fhirLink != null) {
            try {
                Resource resource = fhirResource.get(fhirLink.getResourceId(), ResourceType.valueOf(fhirLink.getResourceType()));
                UUID versionId =  fhirResource.update(resource, fhirLink);
                fhirLink.setVersionId(versionId);
                fhirLinkRepository.save(fhirLink);
            } catch (FhirResourceException e) {
                LOG.error("Could update patient resource ", e);
            }
        }
    }

    private JSONObject create(Patient patient) throws FhirResourceException {
        try {
            return fhirResource.create(patient);
        } catch (Exception e) {
            LOG.error("Could not build patient resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }

    private Identifier matchPatient(Patientview patientview) throws ResourceNotFoundException {
        nhsIdentifier = lookupRepository.findByTypeAndValue(LookupTypes.IDENTIFIER, "NHS_NUMBER");
        Identifier identifier = identifierRepository.findByTypeAndValue(
                patientview.getPatient().getPersonaldetails().getNhsno(), nhsIdentifier);

        if (identifier == null) {
            throw new ResourceNotFoundException("The NHS number is not linked with PatientView");
        }

        return identifier;
    }

    private FhirLink retrieveLink(Group group, Identifier identifier) {
        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                identifier.getUser(), group, identifier);

        if (!fhirLinks.isEmpty()) {
            // return most recent as ordered by created DESC
            return fhirLinks.get(0);
        } else {
            return null;
        }
    }

    private void addLink(Identifier identifier, Group group, JSONObject bundle) {
        if (CollectionUtils.isEmpty(identifier.getUser().getFhirLinks())) {
            identifier.getUser().setFhirLinks(new HashSet<FhirLink>());
        }
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(identifier.getUser());
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(Util.getResourceId(bundle));
        fhirLink.setVersionId((Util.getVersionId(bundle)));
        fhirLink.setResourceType(ResourceType.Patient.name());

        identifier.getUser().getFhirLinks().add(fhirLink);
        userRepository.save(identifier.getUser());
    }


}
