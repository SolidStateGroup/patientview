package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Identifier;
import org.patientview.api.controller.BaseController;
import org.patientview.api.service.EncounterService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Override
    public List<UUID> getUuidsByUserIdAndType(final Long userId, final EncounterTypes encounterType)
            throws ResourceNotFoundException, FhirResourceException {

        List<UUID> encounterUuids = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (encounterType == null) {
            throw new ResourceNotFoundException("Encounter type must be set");
        }

        Connection connection = null;
        String fhirLinkString = "";
        List<FhirLink> fhirLinks = new ArrayList<>(user.getFhirLinks());

        for (int i = 0; i < fhirLinks.size(); i++) {
            FhirLink fhirLink = fhirLinks.get(i);
            if (fhirLink.getActive()) {
                fhirLinkString += "'" + fhirLink.getResourceId().toString() + "'";
                if (i != fhirLinks.size() - 1) {
                    fhirLinkString += ",";
                }
            }
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM encounter ");
        query.append("WHERE content -> 'subject' ->> 'display' IN (");
        query.append(fhirLinkString);
        query.append(") AND content #> '{identifier,0}' -> 'value' = '\"");
        query.append(encounterType.toString());
        query.append("\"'");

        try {
            if (connection == null) {
                connection = dataSource.getConnection();
            }
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            while ((results.next())) {
                encounterUuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException e1) {
                throw new FhirResourceException(e);
            }
        }

        return encounterUuids;
    }

    @Override
    public List<Encounter> get(final UUID patientUuid) throws FhirResourceException {
        List<Encounter> encounters = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    encounter ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(patientUuid);
        query.append("' ");

        encounters.addAll(fhirResource.findResourceByQuery(query.toString(), Encounter.class));

        return encounters;
    }

    @Override
    public void addEncounter(FhirEncounter fhirEncounter, FhirLink fhirLink, UUID organizationUuid)
            throws ResourceNotFoundException, FhirResourceException {

        Encounter encounter = new Encounter();
        encounter.setStatusSimple(Encounter.EncounterState.finished);

        if (StringUtils.isNotEmpty(fhirEncounter.getEncounterType())) {
            Identifier identifier = encounter.addIdentifier();
            identifier.setValueSimple(fhirEncounter.getEncounterType());
        }

        if (StringUtils.isNotEmpty(fhirEncounter.getStatus())) {
            CodeableConcept code = encounter.addType();
            code.setTextSimple(fhirEncounter.getStatus());
        }

        encounter.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));
        encounter.setServiceProvider(Util.createFhirResourceReference(organizationUuid));

        fhirResource.create(encounter);
    }
}
