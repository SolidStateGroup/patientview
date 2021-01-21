package org.patientview.service.impl;

import com.zaxxer.hikari.HikariDataSource;
import generated.Patientview;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.EncounterBuilder;
import org.patientview.builder.EncountersBuilder;
import org.patientview.builder.ObservationBuilder;
import org.patientview.builder.ProcedureBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirProcedure;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.SurgeryObservationTypes;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.EncounterService;
import org.patientview.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
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
    private HikariDataSource dataSource;

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
        // patient reference
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

        // build Encounter
        EncounterBuilder encounterBuilder = new EncounterBuilder(null, fhirEncounter, patientReference,
                Util.createResourceReference(organizationUuid));

        // store Encounter
        FhirDatabaseEntity databaseEntity
                = fhirResource.createEntity(encounterBuilder.build(), ResourceType.Encounter.name(), "encounter");

        // encounter reference
        ResourceReference encounterReference = Util.createResourceReference(databaseEntity.getLogicalId());

        // build and store observations (used for selects and text fields for SURGERY type encounters)
        if (!CollectionUtils.isEmpty(fhirEncounter.getObservations())) {
            for (FhirObservation fhirObservation : fhirEncounter.getObservations()) {
                fhirObservation.setApplies(fhirEncounter.getDate());

                ObservationBuilder observationBuilder
                        = new ObservationBuilder(null, fhirObservation, patientReference, encounterReference);
                fhirResource.createEntity(observationBuilder.build(), ResourceType.Observation.name(), "observation");
            }
        }

        // build and store procedures (used for surgery site, e.g. foot)
        if (!CollectionUtils.isEmpty(fhirEncounter.getProcedures())) {
            for (FhirProcedure fhirProcedure : fhirEncounter.getProcedures()) {
                ProcedureBuilder procedureBuilder
                        = new ProcedureBuilder(null, fhirProcedure, patientReference, encounterReference);
                fhirResource.createEntity(procedureBuilder.build(), ResourceType.Procedure.name(), "procedure");
            }
        }

        return databaseEntity;
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
        List<UUID> encounterUuids = fhirResource.getLogicalIdsBySubjectIdAndIdentifierValue(
                "encounter", subjectId, encounterType.toString());

        if (!CollectionUtils.isEmpty(encounterUuids)) {
            String encounterUuidString = "";

            for (int i = 0; i < encounterUuids.size(); i++) {
                encounterUuidString += "'" + encounterUuids.get(i).toString() + "'";
                if (i != encounterUuids.size() - 1) {
                    encounterUuidString += ",";
                }
            }

            // delete Encounters
            fhirResource.executeSQL(
                "DELETE FROM encounter WHERE logical_id IN (" + encounterUuidString + ");"
            );

            // delete associated Procedures
            fhirResource.executeSQL(
                "DELETE FROM procedure WHERE CONTENT -> 'encounter' ->> 'display' IN (" + encounterUuidString + ");"
            );

            // optimised delete observations (get by {performer, 0} is too slow), more queries but overall quicker
            List<String> surgeryObservationNames = new ArrayList<>();
            for (SurgeryObservationTypes surgeryObservationType : SurgeryObservationTypes.values()) {
                surgeryObservationNames.add(surgeryObservationType.toString());
                surgeryObservationNames.add(surgeryObservationType.toString());
            }

            // get logical id of all patient's observations with surgery names
            List<UUID> surgeryObservationUuids = fhirResource.getLogicalIdsBySubjectIdAndNames("observation",
                    subjectId, surgeryObservationNames);

            if (!CollectionUtils.isEmpty(surgeryObservationUuids)) {
                List<UUID> toDelete = new ArrayList<>();

                // get UUID of all Observations associated with found Encounters
                for (UUID encounterUuid : encounterUuids) {
                    for (UUID observationUuid : surgeryObservationUuids) {
                        // get observation
                        Observation observation
                                = (Observation) fhirResource.get(observationUuid, ResourceType.Observation);

                        // if performer is encounter UUID add to list to be deleted
                        if (observation != null && !CollectionUtils.isEmpty(observation.getPerformer())
                                && StringUtils.isNotEmpty(
                                observation.getPerformer().get(0).getDisplaySimple())
                                && observation.getPerformer().get(0).getDisplaySimple().equals(
                                encounterUuid.toString())) {
                            toDelete.add(observationUuid);
                        }
                    }
                }

                // delete
                if (!CollectionUtils.isEmpty(toDelete)) {
                    for (UUID logicalId : toDelete) {
                        fhirResource.deleteEntity(logicalId, "observation");
                    }
                }
            }
        }
    }

    @Override
    public void deleteByUserAndType(final User user, final EncounterTypes encounterType) throws FhirResourceException {
        if (encounterType == null) {
            throw new FhirResourceException("Encounter type must be set");
        }

        Connection connection = null;
        java.sql.Statement statement = null;

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
            statement = connection.createStatement();
            statement.executeQuery(query.toString());
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e1) {
                throw new FhirResourceException(e);
            }
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
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
