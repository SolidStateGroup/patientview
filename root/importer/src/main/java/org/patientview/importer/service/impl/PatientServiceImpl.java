package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.builder.PatientBuilder;
import org.patientview.importer.exception.FhirResourceException;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.util.Util;
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
import java.sql.SQLException;
import java.util.HashSet;

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
    public void add(final Patientview patient) throws ResourceNotFoundException, FhirResourceException {
        // Find the identifier which the patient is linked to.
        Identifier identifier = matchPatient(patient);

        // Find the group that is importing the data
        Group group = groupRepository.findByCode(patient.getCentredetails().getCentrecode());

        // Find and delete the link between the existing User and UNit to the Fhir Record
        FhirLink fhirLink = retrieveLink(group, identifier);
        delete(fhirLink);

        // Create a new Fhir record and add the link to the User and Unit
        JSONObject jsonObject = create(PatientBuilder.create(patient));
        addLink(identifier, group, jsonObject);

        LOG.info("Processed Patient NHS number: " + patient.getPatient().getPersonaldetails().getNhsno());
    }

    private void delete(FhirLink fhirLink) {
        if (fhirLink != null) {
            try {
                fhirResource.delete(fhirLink.getResourceId(), ResourceType.Patient);
            } catch (SQLException | FhirResourceException e) {
                LOG.error("Could delete patient resource ", e);
            }
            fhirLinkRepository.delete(fhirLink);
        }
    }

    private JSONObject create(Patient patient) throws FhirResourceException {
        try {
            return fhirResource.create(patient);
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

    private FhirLink retrieveLink(Group group, Identifier identifier) {
        return fhirLinkRepository.findByUserAndGroupAndIdentifier(identifier.getUser(), group, identifier);
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
