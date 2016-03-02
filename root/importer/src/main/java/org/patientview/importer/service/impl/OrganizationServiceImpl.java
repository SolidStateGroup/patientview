package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.builder.OrganizationBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.OrganizationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.repository.GroupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class OrganizationServiceImpl extends AbstractServiceImpl<OrganizationService> implements OrganizationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    private String nhsno;

    /**
     * Creates FHIR organization (unit/centre) record from the Patientview object.
     *
     * @param data
     */
    @Override
    public UUID add(final Patientview data) throws ResourceNotFoundException, FhirResourceException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        //LOG.info(nhsno + ": Starting Organization Process");

        // validate that group exists in patientview using persistence module, otherwise throw exception
        if (!groupWithCodeExists(data.getCentredetails().getCentrecode())) {
            LOG.error(nhsno + ": Unable to build organization, group not found");
            throw new ResourceNotFoundException(nhsno + ": Unable to build organization, group not found");
        }

        OrganizationBuilder organizationBuilder = new OrganizationBuilder(data);

        // create if not already in FHIR, otherwise update
        try {
            // build FHIR object
            Organization importOrganization = organizationBuilder.build();
            List<Map<String, UUID>> uuids = getUuidsByCode(data.getCentredetails().getCentrecode());

            if (!uuids.isEmpty()) {
                // native update existing FHIR entities (should be a single row)
                UUID logicalId = null;

                for (Map<String, UUID> objectData : uuids) {
                    fhirResource.updateEntity(importOrganization,
                            ResourceType.Organization.name(), "organization", objectData.get("logicalId"));
                    logicalId = objectData.get("logicalId");
                }

                LOG.info(nhsno + ": Existing Organization, " + logicalId);
                return logicalId;
            } else {
                // native create new FHIR organization
                FhirDatabaseEntity entity
                    = fhirResource.createEntity(importOrganization, ResourceType.Organization.name(), "organization");
                LOG.info(nhsno + ": New Organization, " + entity.getLogicalId());
                return entity.getLogicalId();
            }
        } catch (FhirResourceException e) {
            LOG.error(nhsno + ": Unable to save organization: " + e.getMessage());
            throw e;
        }
    }

    public boolean groupWithCodeExists(String code) {
        return (groupRepository.findByCode(code) != null);
    }

    private List<Map<String, UUID>> getUuidsByCode(final String code) throws FhirResourceException {
        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT version_id, logical_id ");
        query.append("FROM organization ");
        query.append("WHERE content #> '{identifier,0}' -> 'value' = '\"");
        query.append(code);
        query.append("\"' ");

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            List<Map<String, UUID>> uuids = new ArrayList<>();

            while ((results.next())) {
                Map<String, UUID> ids = new HashMap<>();
                ids.put("versionId", UUID.fromString(results.getString(1)));
                ids.put("logicalId", UUID.fromString(results.getString(2)));
                uuids.add(ids);
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }
}


