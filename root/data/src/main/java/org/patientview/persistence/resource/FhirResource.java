package org.patientview.persistence.resource;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
 * Created on 02/09/2014
 */
@Service
public class FhirResource {

    private final Logger LOG = LoggerFactory.getLogger(FhirResource.class);
    private static final String  config =  "{\"base\":\"http:/myserver\"}";
    private static final JsonParser jsonParser = new JsonParser();

    @Inject
    @Named("fhir")
    private DataSource dataSource;

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

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource Resource to create
     * @return JSONObject JSON version of saved Resource
     * @throws FhirResourceException
     */
    public JSONObject create(Resource resource) throws FhirResourceException {
        //LOG.info("c1 " + new Date().getTime());
        PGobject result;
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());

            proc.close();
            connection.close();
            return jsonObject;
        } catch (SQLException e) {
            LOG.error("Unable to build resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2);
            }

            throw new FhirResourceException(e);
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource Resource to create
     * @return JSONObject JSON version of saved Resource
     * @throws FhirResourceException
     */
    public void createFast(Resource resource) throws FhirResourceException {
        //LOG.info("c1 " + new Date().getTime());
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            //LOG.info("c2 " + new Date().getTime());
            CallableStatement proc = connection.prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.execute();
            //LOG.info("c3 " + new Date().getTime());
            proc.close();
            connection.close();
            //LOG.info("c4 " + new Date().getTime());
        } catch (SQLException e) {
            LOG.error("Unable to build resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2);
            }

            throw new FhirResourceException(e);
        } catch (Exception e) {
            throw new FhirResourceException(e);
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
            proc.setObject(5, marshallFhirRecord(resource));
            proc.setObject(6, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
            connection.close();
            return jsonObject;

        } catch (SQLException e) {
            // will likely fail if trying to update the same resource in multiple threads
            LOG.error("Unable to update resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
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
            proc.setObject(5, marshallFhirRecord(resource));
            proc.setObject(6, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
            connection.close();
            return getVersionId(jsonObject);

        } catch (SQLException e) {
            LOG.error("Unable to update resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    /**
     * For FUNCTION fhir_delete(cfg jsonb, _type varchar, id uuid)
     *
     */
    public void delete(UUID uuid, ResourceType resourceType) throws FhirResourceException {

        //LOG.debug("Delete {} resource {}", resourceType.toString(), uuid.toString());
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            CallableStatement proc = connection.prepareCall("{call fhir_delete( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);
            proc.execute();
            connection.close();
        } catch (SQLException e) {
            LOG.error("Unable to delete resource {}", e);
            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }
        }
    }

    public JSONObject getResource(UUID uuid, ResourceType resourceType) throws FhirResourceException {
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
        } catch (SQLException e) {
            LOG.error("Unable to retrieve resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public <T extends Resource> Object getResourceConverted(UUID uuid, ResourceType resourceType)
            throws FhirResourceException{
        JSONObject object = getResource(uuid, resourceType);
        try {
            return (T) jsonParser.parse(new ByteArrayInputStream(object.toString().getBytes()));
        } catch (Exception e) {
            throw new FhirResourceException("Cannot convert resource");
        }
    }

    public <T extends Resource> List<T> findResourceByQuery(String sql, Class<T> resourceType)
            throws FhirResourceException {

        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);
            List<T> resultsList = convertResultSet(results);
            connection.close();
            return resultsList;
        } catch (SQLException e) {
            LOG.error("Unable to find resource resource by query {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

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

    public List<String[]> findLatestObservationsByQuery(String sql) throws FhirResourceException {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            List<String[]> observations = new ArrayList<>();

            while ((results.next())) {
                String[] res = {results.getString(1), results.getString(2), results.getString(3), results.getString(4)};
                observations.add(res);
            }

            connection.close();
            return observations;
        } catch (SQLException e) {
            LOG.error("Unable to find latest observations by query {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public List<UUID> getLogicalIdsBySubjectId(final String tableName, final UUID subjectId)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");

        Connection connection = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            LOG.error("Unable to get logical ids by subject id {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public List<UUID> getLogicalIdsBySubjectIdAppliesIgnoreNames(
            final String tableName, final UUID subjectId,
            final List<String> namesToIgnore, final Long start, final Long end) throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content->'appliesDateTime' ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");

        if (!CollectionUtils.isEmpty(namesToIgnore)) {
            // names to ignore
            query.append("AND UPPER(content -> 'name' ->> 'text') NOT IN (");
            for (int i = 0; i < namesToIgnore.size(); i++) {
                //query.append("'\"").append(namesToIgnore.get(i).toUpperCase()).append("\"'");
                query.append("'").append(namesToIgnore.get(i).toUpperCase()).append("'");

                if (i != (namesToIgnore.size() - 1)) {
                    query.append(",");
                }
            }

            query.append(") ");
        }

        // todo: better way of getting results? possible slowdown due to large table?
        /*if (start != null && end != null) {
            query.append("AND (to_timestamp(content -> 'appliesDateTime') > ");
            query.append(start);
            query.append(" AND to_timestamp(content -> 'appliesDateTime') < ");
            query.append(end);
            query.append(")");
        }*/


        Connection connection = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {

                if (start != null && end != null) {
                    // only return UUID if within start to end time
                    if (StringUtils.isNotEmpty(results.getString(2))) {
                        try {
                            String dateString = results.getString(2).replace("\"", "");
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            Long applies = xmlDate.toGregorianCalendar().getTime().getTime();

                            if (start <= applies && end >= applies) {
                                uuids.add(UUID.fromString(results.getString(1)));
                            }
                        } catch (DatatypeConfigurationException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                } else {
                    uuids.add(UUID.fromString(results.getString(1)));
                }

            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            LOG.error("Unable to get logical ids by subject id {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public List<UUID> getLogicalIdsBySubjectIdAndNames(
            final String tableName, final UUID subjectId, final List<String> names) throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content->'appliesDateTime' ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");

        if (!CollectionUtils.isEmpty(names)) {
            // names to ignore
            query.append("AND UPPER(content -> 'name' ->> 'text') IN (");
            for (int i = 0; i < names.size(); i++) {
                query.append("'").append(names.get(i).toUpperCase()).append("'");

                if (i != (names.size() - 1)) {
                    query.append(",");
                }
            }

            query.append(") ");
        } else {
            throw new FhirResourceException("Must have names");
        }

        Connection connection = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            LOG.error("Unable to get logical ids by subject id {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public List<UUID> getLogicalIdsByPatientId(final String tableName, final UUID subjectId)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE content -> 'patient' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");

        Connection connection = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            LOG.error("Unable to retrieve resource {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    private JSONObject getBundle(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        //LOG.debug("Getting {} resource {}", resourceType.toString(), uuid.toString());
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
        } catch (SQLException e) {
            LOG.error("Unable to get bundle {}", e);

            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public void executeSQL(String sql) throws FhirResourceException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.close();
        } catch (SQLException e) {
            LOG.error("SQL exception: " + sql);
            LOG.error("SQL exception:", e);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }
    }

    public String marshallFhirRecord(Resource resource) throws FhirResourceException {
        JsonComposer jsonComposer = new JsonComposer();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            jsonComposer.compose(outputStream, resource, false);
        } catch (Exception e) {
            LOG.error("Unable to handle Fhir resource record", e);
            throw new FhirResourceException("Cannot build JSON", e);
        }
        return outputStream.toString();
    }

    public static UUID getVersionId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        JSONArray links = (JSONArray) resource.get("link");
        JSONObject link = (JSONObject)  links.get(0);
        String[] href = link.getString("href").split("/");
        return UUID.fromString(href[href.length - 1]);
    }

    public static UUID getLogicalId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.getString("id"));
    }
}
