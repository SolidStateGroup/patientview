package org.patientview.importer.resource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.importer.util.Util;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
@Component
public class FhirResource {

    private final Logger LOG = LoggerFactory.getLogger(FhirResource.class);
    private static final String  config =  "{\"base\":\"http:/myserver\"}";
    private static final JsonParser jsonParser = new JsonParser();

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    @PostConstruct
    public void init() {

    }

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource Resource to create
     * @return JSONObject JSON version of saved Resource
     * @throws FhirResourceException
     */
    public JSONObject create(Resource resource) throws FhirResourceException {

        PGobject result;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, Util.marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            connection.close();
            return new JSONObject(result.getValue());

        } catch (SQLException e) {
            LOG.error("Unable to build resource {}", e);
            throw new FhirResourceException(e.getMessage());
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }

    }


    /**
     *
     * FUNCTION fhir_update(cfg jsonb, _type varchar, id uuid, vid uuid, resource jsonb, tags jsonb)
     *
     */
    public UUID update(Resource resource, FhirLink fhirLink) throws FhirResourceException {

        PGobject result;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_update( ?::jsonb, ?, ?, ?,  ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, fhirLink.getResourceId());
            proc.setObject(4, fhirLink.getVersionId());
            proc.setObject(5, Util.marshallFhirRecord(resource));
            proc.setObject(6, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
            connection.close();
            return Util.getVersionId(jsonObject);

        } catch (SQLException e) {
            LOG.error("Unable to update resource {}", e);
            throw new FhirResourceException(e.getMessage());
        }

    }


    /**
     *
     * FUNCTION fhir_update(cfg jsonb, _type varchar, id uuid, vid uuid, resource jsonb, tags jsonb)
     *
     */
    public JSONObject updateFhirObject(Resource resource, UUID resourceId, UUID versionId) throws FhirResourceException {

        PGobject result;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_update( ?::jsonb, ?, ?, ?,  ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, resourceId);
            proc.setObject(4, versionId);
            proc.setObject(5, Util.marshallFhirRecord(resource));
            proc.setObject(6, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
            connection.close();
            return jsonObject;

        } catch (SQLException e) {
            LOG.error("Unable to update resource {}", e);
            throw new FhirResourceException(e.getMessage());
        }
    }

    /**
     * For FUNCTION fhir_delete(cfg jsonb, _type varchar, id uuid)
     *
     */
    public void delete(UUID uuid, ResourceType resourceType) throws SQLException, FhirResourceException {

        LOG.debug("Delete resource {}", uuid.toString());
        Connection connection = dataSource.getConnection();
        CallableStatement proc  = connection.prepareCall("{call fhir_delete( ?::jsonb, ?, ?)}");
        proc.setObject(1, config);
        proc.setObject(2, resourceType.name());
        proc.setObject(3, uuid);
        proc.execute();
        connection.close();
    }


    private JSONObject getBundle(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        LOG.debug("Getting resource {}", uuid.toString());
        PGobject result;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_read( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();
            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
            connection.close();
            return jsonObject;
        } catch (Exception e) {
            // Fhir parser just throws exception
            LOG.error("Could not retrieve resource");
            throw new FhirResourceException(e.getMessage());
        }
    }

    public Resource get(UUID uuid, ResourceType resourceType) throws FhirResourceException {

        JSONObject jsonObject = getBundle(uuid, resourceType);
        JSONArray resultArray = (JSONArray) jsonObject.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);

        try {
            return jsonParser.parse(new ByteArrayInputStream(resource.getJSONObject("content").toString().getBytes()));
        } catch (Exception e) {
            throw new FhirResourceException(e.getMessage());
        }
    }

    private <T> List<T> convertResultSet(ResultSet resultSet) throws SQLException {
        List<T> resources = new ArrayList<>();
        while ((resultSet.next())) {
            try {
                T resource = (T) jsonParser.parse(new ByteArrayInputStream(resultSet.getString(1).getBytes()));
                resources.add(resource);
            } catch (Exception e) {
                LOG.error("Cannot create resource");
            }
        }
        return resources;
    }

    public List<UUID> getLogicalIdsBySubjectId(final String tableName, final UUID subjectId)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE   content ->> 'subject' = '{\"display\": \"");
        query.append(subjectId);
        query.append("\", \"reference\": \"uuid\"}' ");

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }

    public List<UUID> getLogicalIdsByPatientId(final String tableName, final UUID subjectId)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE   content ->> 'patient' = '{\"display\": \"");
        query.append(subjectId);
        query.append("\", \"reference\": \"uuid\"}' ");

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }
}