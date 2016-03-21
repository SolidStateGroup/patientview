package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.EncounterBuilder;
import org.patientview.builder.EncountersBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.EncounterService;
import org.patientview.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class EncounterServiceImpl extends AbstractServiceImpl<EncounterService> implements EncounterService{

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    private String nhsno;

    /**
     * Creates FHIR encounter (treatment and transplant details) records from the Patientview object.
     *
     * @param data
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink, final ResourceReference groupReference)
            throws FhirResourceException, SQLException {
        LOG.trace(nhsno + ": Starting Encounter Process");

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        EncountersBuilder encountersBuilder = new EncountersBuilder(data, patientReference, groupReference);

        // delete existing
        deleteBySubjectIdAndType(fhirLink.getResourceId(), EncounterTypes.TREATMENT);
        deleteBySubjectIdAndType(fhirLink.getResourceId(), EncounterTypes.TRANSPLANT_STATUS);

        int count = 0;
        for (Encounter encounter : encountersBuilder.build()) {
            LOG.trace(nhsno + ": Creating... encounter " + count);
            try {
                fhirResource.createEntity(encounter, ResourceType.Encounter.name(), "encounter");
            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to build encounter");
            }
            LOG.trace(nhsno + ": Finished creating encounter " + count++);
        }
        LOG.info(nhsno + ": Processed {} of {} encounters", encountersBuilder.getSuccess(),
                encountersBuilder.getCount());
    }

    @Override
    public FhirDatabaseEntity add(FhirEncounter fhirEncounter, FhirLink fhirLink, UUID organizationUuid)
            throws FhirResourceException {
        EncounterBuilder encounterBuilder = new EncounterBuilder(null, fhirEncounter,
                Util.createResourceReference(fhirLink.getResourceId()),
                Util.createResourceReference(organizationUuid));

        return fhirResource.createEntity(encounterBuilder.build(), ResourceType.Encounter.name(), "encounter");
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        // do not delete EncounterType TRANSPLANT_STATUS_KIDNEY or TRANSPLANT_STATUS_PANCREAS
        // as these come from uktstatus table during migration, delete encounter natively
        fhirResource.executeSQL(
            "DELETE FROM encounter WHERE CONTENT -> 'subject' ->> 'display' = '"
            + subjectId.toString()
            + "' AND CONTENT #> '{identifier,0}' ->> 'value' NOT IN ('"
            + EncounterTypes.SURGERY.toString() + "','"
            + EncounterTypes.TRANSPLANT_STATUS_KIDNEY.toString() + "','"
            + EncounterTypes.TRANSPLANT_STATUS_PANCREAS.toString() + "');"
        );
    }

    @Override
    public void deleteBySubjectIdAndType(UUID subjectId, EncounterTypes encounterType)
            throws FhirResourceException {
        fhirResource.executeSQL(
            "DELETE FROM encounter WHERE CONTENT -> 'subject' ->> 'display' = '"
            + subjectId.toString()
            + "' AND CONTENT #> '{identifier,0}' ->> 'value' ='"
            + encounterType.toString() + "';"
        );
    }

    @Override
    public void deleteByUserAndType(final User user, final EncounterTypes encounterType) throws FhirResourceException {
        if (encounterType == null) {
            throw new FhirResourceException("Encounter type must be set");
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

        // if no fhirLinks return empty array
        if (StringUtils.isEmpty(fhirLinkString)) {
            return;
        }

        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM encounter ");
        query.append("WHERE content -> 'subject' ->> 'display' IN (");
        query.append(fhirLinkString);
        query.append(") AND content #> '{identifier,0}' -> 'value' = '\"");
        query.append(encounterType.toString());
        query.append("\"'");

        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            statement.executeQuery(query.toString());
            connection.close();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e1) {
                throw new FhirResourceException(e);
            }
        }
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
}


