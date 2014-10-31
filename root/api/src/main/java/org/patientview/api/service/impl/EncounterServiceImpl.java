package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Identifier;
import org.patientview.api.controller.BaseController;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.api.service.EncounterService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class EncounterServiceImpl extends BaseController<EncounterServiceImpl> implements EncounterService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<FhirEncounter> get(final Long userId, final String code)
            throws ResourceNotFoundException, FhirResourceException {

        List<Encounter> encounters = new ArrayList<>();
        List<FhirEncounter> fhirEncounters = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    encounter ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\", \"reference\": \"uuid\"}'");
                encounters.addAll(fhirResource.findResourceByQuery(query.toString(), Encounter.class));
            }
        }

        // convert to transport encounters
        for (Encounter encounter : encounters) {
            fhirEncounters.add(new FhirEncounter(encounter));
        }
        return fhirEncounters;
    }

    @Override
    public List<Encounter> get(final UUID patientUuid) throws FhirResourceException {
        List<Encounter> encounters = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    encounter ");
        query.append("WHERE   content->> 'subject' = '{\"display\": \"");
        query.append(patientUuid);
        query.append("\", \"reference\": \"uuid\"}'");
        encounters.addAll(fhirResource.findResourceByQuery(query.toString(), Encounter.class));

        return encounters;
    }

    @Override
    public void addEncounter(FhirEncounter fhirEncounter, FhirLink fhirLink, UUID organizationUuid)
            throws ResourceNotFoundException, FhirResourceException {

        Encounter encounter = new Encounter();
        encounter.setStatusSimple(Encounter.EncounterState.finished);
        Identifier identifier = encounter.addIdentifier();
        identifier.setValueSimple(fhirEncounter.getEncounterType());

        CodeableConcept code = encounter.addType();
        code.setTextSimple(fhirEncounter.getStatus());

        encounter.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));
        encounter.setServiceProvider(Util.createFhirResourceReference(organizationUuid));

        fhirResource.create(encounter);
    }
}
