package org.patientview.persistence.resource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpPatient;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private HikariDataSource dataSource;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private IdentifierRepository identifierRepository;

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

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource Resource to create
     * @return JSONObject JSON version of saved Resource
     * @throws FhirResourceException
     */
    @Deprecated
    public JSONObject create(Resource resource) throws FhirResourceException {
        PGobject result;
        Connection connection = null;
        CallableStatement proc = null;

        try {
            connection = dataSource.getConnection();
            proc = connection.prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());

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
        } finally {
            try {
                if (proc != null) {
                    proc.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close CallableStatement {}", e2);
            }

            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Natively update FHIR entity, returning newly created Resource logical UUID.
     * @param resource Resource to create, e.g. Observation, Patient, etc
     * @param resourceType Type of the Resource, e.g. "Observation", "Patient" etc
     * @param tableName Table name, e.g. "observation", "patient" etc
     * @return FhirDatabaseEntity, used to create newly created Resource
     * @throws FhirResourceException
     */
    public FhirDatabaseEntity createEntity(Resource resource, String resourceType, String tableName)
            throws FhirResourceException {
        FhirDatabaseEntity entity = new FhirDatabaseEntity(marshallFhirRecord(resource), resourceType);
        entity.setLogicalId(UUID.randomUUID());
        entity.setPublished(entity.getUpdated());

        executeSQL("INSERT INTO " + tableName +
                " (logical_id, version_id, resource_type, published, updated, content) VALUES " +
                "('" + entity.getLogicalId() + "'," +
                "'" + entity.getVersionId() + "'," +
                "'" + entity.getResourceType() + "'," +
                "'" + entity.getPublished() + "'," +
                "'" + entity.getUpdated() + "'," +
                "'" + CommonUtils.cleanSql(entity.getContent()) + "')");

        return entity;
    }

    /**
     * For FUNCTION fhir_delete(cfg jsonb, _type varchar, id uuid)
     */
    @Deprecated
    public void delete(UUID uuid, ResourceType resourceType) throws FhirResourceException {

        //LOG.debug("Delete {} resource {}", resourceType.toString(), uuid.toString());
        Connection connection = null;
        CallableStatement proc = null;
        try {
            connection = dataSource.getConnection();
            proc = connection.prepareCall("{call fhir_delete( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);

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
        } finally {
            try {
                if (proc != null) {
                    proc.close();
                }
            } catch (SQLException e) {
                LOG.error("Cannot close CallableStatement {}", e);
            }

            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Simple delete statement to remove entity based on logical id and table name.
     * @param logicalId Logical UUID of the object to delete, the primary key
     * @param tableName Table name, e.g. "observation", "patient" etc
     * @throws FhirResourceException
     */
    public void deleteEntity(UUID logicalId, String tableName) throws FhirResourceException {
        executeSQL("DELETE FROM " + tableName + " WHERE logical_id = '" + logicalId + "'");
    }

    public void executeSQL(String sql) throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);

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
        } catch (Exception e) {
            LOG.error("executeSQL Exception: " + sql);
            LOG.error("executeSQL Exception:", e);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }

    @Transactional(readOnly = true)
    public List<String[]> findLatestObservationsByQuery(String sql) throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(sql);

            List<String[]> observations = new ArrayList<>();

            while ((results.next())) {
                String[] res = {results.getString(1), results.getString(2),
                        results.getString(3), results.getString(4), results.getString(5)};
                observations.add(res);
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    /**
     * Execute query to return a list of array values.
     * Depending on the query created, the number of fields to find, should map to
     * the size of the array values to return
     *
     * @param sql sql to execute
     * @param size size of the array values to map
     * @return a list of array values
     * @throws FhirResourceException
     */
    @Transactional(readOnly = true)
    public List<String[]> findValuesByQueryAndArray(String sql, int size) throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(sql);

            List<String[]> observations = new ArrayList<>();

            while ((results.next())) {
                String[] res = new String[size];
                // array of values to return from 0 to n, result to retrieve from 1 to n+1
                for (int i = 0; i < size; i++) {
                    res[i] = results.getString(i + 1);
                }
                observations.add(res);
            }

            return observations;
        } catch (SQLException e) {
            LOG.error("Unable to find values by query {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public <T extends Resource> List<T> findResourceByQuery(String sql, Class<T> resourceType)
            throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(sql);
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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public Resource get(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        JSONObject jsonObject = getBundle(uuid, resourceType);

        // return null if not found
        if (jsonObject.get("entry").equals(JSONObject.NULL)) {
            return null;
        }

        JSONArray resultArray = (JSONArray) jsonObject.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);

        try {
            return jsonParser.parse(new ByteArrayInputStream(resource.getJSONObject("content").toString().getBytes()));
        } catch (Exception e) {
            throw new FhirResourceException(e.getMessage());
        }
    }

    private JSONObject getBundle(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        //LOG.debug("Getting {} resource {}", resourceType.toString(), uuid.toString());
        PGobject result;
        Connection connection = null;
        CallableStatement proc = null;
        try {
            connection = dataSource.getConnection();
            proc = connection.prepareCall("{call fhir_read( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();
            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());

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
        } finally {
            try {
                if (proc != null) {
                    proc.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close CallableStatement {}", e2);
            }

            DbUtils.closeQuietly(connection);
        }
    }

    public List<UUID> getConditionLogicalIds(
            final UUID subjectId, final String category, final String severity, final String code)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM condition WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");

        if (StringUtils.isNotEmpty(category)) {
            query.append("AND content -> 'category' ->> 'text' = '");
            query.append(category);
            query.append("' ");
        }

        if (StringUtils.isNotEmpty(severity)) {
            query.append("AND content -> 'severity' ->> 'text' = '");
            query.append(severity);
            query.append("' ");
        }

        if (StringUtils.isNotEmpty(code)) {
            query.append("AND content -> 'code' ->> 'text' = '");
            query.append(code);
            query.append("' ");
        }

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public Long getCountEncounterBySubjectIdsAndCodes(List<UUID> subjectIds, List<String> codeList)
            throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        Long result;

        // convert list of UUID and code to suitable string
        String uuids = "'" + StringUtils.join(subjectIds, "','") + "'";
        String codes = "'" + StringUtils.join(codeList, "','") + "'";

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(DISTINCT content -> 'subject' ->> 'display') ");
        query.append("FROM encounter ");
        query.append("WHERE content -> 'subject' ->> 'display' IN (").append(uuids).append(") ");
        query.append("AND content #> '{type,0}'->>'text' IN (").append(codes).append(") ");
        query.append("AND content #> '{identifier,0}'->>'value' = 'TREATMENT'");

        //LOG.info(query.toString());

        // execute and return map of logical ids and applies
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            // get a single result
            results.next();
            result = results.getLong(1);

        } catch (SQLException e) {
            LOG.error("Unable to get encounter counts: {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return result;
    }

    public Long getCountEncounterBySubjectIdsAndNotCodes(List<UUID> subjectIds, List<String> codeList)
            throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        Long result;

        // convert list of UUID and code to suitable string
        String uuids = "'" + StringUtils.join(subjectIds, "','") + "'";
        String codes = "'" + StringUtils.join(codeList, "','") + "'";

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(DISTINCT content -> 'subject' ->> 'display') ");
        query.append("FROM encounter ");
        query.append("WHERE content -> 'subject' ->> 'display' IN (").append(uuids).append(") ");
        query.append("AND content #> '{type,0}'->>'text' NOT IN (").append(codes).append(") ");
        query.append("AND content #> '{identifier,0}'->>'value' = 'TREATMENT'");

        //LOG.info(query.toString());

        // execute and return map of logical ids and applies
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            // get a single result
            results.next();
            result = results.getLong(1);

        } catch (SQLException e) {
            LOG.error("Unable to get encounter counts: {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return result;
    }

    public Long getCountEncounterTreatmentBySubjectIds(List<UUID> subjectIds)
            throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        Long result;

        // convert list of UUID and code to suitable string
        String uuids = "'" + StringUtils.join(subjectIds, "','") + "'";

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(DISTINCT content -> 'subject' ->> 'display') ");
        query.append("FROM encounter ");
        query.append("WHERE content -> 'subject' ->> 'display' IN (").append(uuids).append(") ");
        query.append("AND content #> '{identifier,0}'->>'value' = 'TREATMENT'");

        //LOG.info(query.toString());

        // execute and return map of logical ids and applies
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            // get a single result
            results.next();
            result = results.getLong(1);

        } catch (SQLException e) {
            LOG.error("Unable to get encounter counts: {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return result;
    }

    public Map<String, String> getDocumentReferenceUuidAndMediaUuid(UUID subjectId, String fhirClass, Date created)
            throws FhirResourceException {
        Map<String, String> existingMap = new HashMap<>();
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content ->> 'location' ");
        query.append("FROM documentreference ");
        query.append("WHERE content -> 'subject' ->> 'display' = '").append(subjectId).append("' ");
        query.append("AND content -> 'class' ->> 'text' = '").append(fhirClass).append("' ");
        query.append("AND CAST(content ->> 'created' AS TIMESTAMP) = '").append(created).append("' ");

        // execute and return map of logical ids and applies
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.executeQuery(query.toString());

            while ((results.next())) {
                existingMap.put(results.getString(1), results.getString(2));
            }

        } catch (SQLException e) {
            LOG.error("Unable to get location uuids by logical id {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return existingMap;
    }

    public Map<String, List<String>> getAllEncounterTreatments() throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT content -> 'subject' ->> 'display', content #> '{type,0}'->>'text' ");
        query.append("FROM Encounter WHERE content #> '{identifier,0}'->>'value' = 'TREATMENT' ");

        //LOG.info(query.toString());

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            Map<String, List<String>> toReturn = new HashMap<>();

            while ((results.next())) {

                if (toReturn.get(results.getString(1)) == null) {
                    toReturn.put(results.getString(1), new ArrayList<String>());
                }

                toReturn.get(results.getString(1)).add(results.getString(2));
            }

            return toReturn;
        } catch (SQLException e) {
            LOG.error("Unable to retrieve {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    // check for existing by letter content, letter has no class
    public Map<String, String> getExistingLetterDocumentReferenceTypeAndContentBySubjectId(UUID resourceId)
            throws FhirResourceException {
        Map<String, String> existingMap = new HashMap<>();
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content -> 'type' ->> 'text', content ->> 'description' ");
        query.append("FROM documentreference ");
        query.append("WHERE content -> 'subject' ->> 'display' = '");
        query.append(resourceId);
        query.append("' ");
        query.append("AND (content ->> 'class') IS NULL");

        // execute and return map of logical ids and applies
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            while ((results.next())) {
                if (StringUtils.isNotEmpty(results.getString(2)) && StringUtils.isNotEmpty(results.getString(3))) {
                    // replace used to fix migrated letters coming as duplicates
                    String content = results.getString(3)
                            .replaceAll("\\s+", " ")
                            .replace("'", "''").replace("''''", "''")
                            .replace(" \n", "CARRIAGE_RETURN").replace("\n ", "CARRIAGE_RETURN")
                            .replace("CARRIAGE_RETURN", "\n");
                    existingMap.put(results.getString(1), results.getString(2) + content);
                }
            }

        } catch (SQLException e) {
            LOG.error("Unable to get location uuids by logical id {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return existingMap;
    }

    /**
     * Get relevant details of PatientView users (name, identifiers, gp name) by identifying FHIR practitioners by
     * postcode. Once FHIR practitioners are found, get all FHIR patients with that practitioner then find
     * User details for those FHIR patients.
     * @param gpPostcode String postcode of FHIR practitioner
     * @return List of GpPatient containing relevant patient details
     */
    public List<GpPatient> getGpPatientsFromPostcode(String gpPostcode) {
        List<GpPatient> gpPatients = new ArrayList<>();
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        try {
            // get logical_id and name of GP from FHIR practitioners where postcode matches
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query = "SELECT logical_id, CONTENT -> 'name' #>> '{family,0}' " +
                    "FROM practitioner " +
                    "WHERE CONTENT -> 'address' ->> 'zip' = '" + gpPostcode  + "' " +
                    "OR CONTENT -> 'address' ->> 'zip' = '" + gpPostcode.replace(" ", "")  + "' " +
                    "GROUP BY logical_id";

            results = statement.executeQuery(query);

            Map<String, Map<String, String>> practitionerMap = new HashMap<>();

            // now have list of practitioners with the postcode, iterate through and add to practitioner map
            while ((results.next())) {
                String logicalId = results.getString(1);
                String name = results.getString(2);

                if (StringUtils.isNotEmpty(logicalId) && StringUtils.isNotEmpty(name)) {
                    Map<String, String> practitioner = new HashMap<>();
                    practitioner.put("logicalId", logicalId);
                    practitioner.put("name", name);
                    practitionerMap.put(logicalId, practitioner);
                }
            }

            connection.close();

            // have map of practitioner logical ids so get patient list for that practitioner,
            // then Users with a FhirLink with each resource id and add relevant User details to gpPatients
            if (!practitionerMap.isEmpty()) {
                for (String practitionerLogicalId : practitionerMap.keySet()) {
                    List<UUID> patientResourceIds = new ArrayList<>();

                    // now have map of practitioners, get list of all patients with that practitioner
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    query = "SELECT logical_id FROM patient WHERE CONTENT #> '{careProvider, 0}' ->> 'display' = '" +
                            practitionerLogicalId + "' GROUP BY logical_id";
                    results = statement.executeQuery(query);

                    while ((results.next())) {
                        if (StringUtils.isNotEmpty(results.getString(1))) {
                            patientResourceIds.add(UUID.fromString(results.getString(1)));
                        }
                    }

                    connection.close();

                    // get Users from FhirLinks based on patient resource ids
                    if (!patientResourceIds.isEmpty()) {
                        for (User user : fhirLinkRepository.findFhirLinkUsersByResourceIds(patientResourceIds)) {
                            // add relevant details from patient users to list of GpPatient to return with GpDetails
                            GpPatient patient = new GpPatient();
                            patient.setId(user.getId());
                            patient.setGpName(
                                    practitionerMap.get(practitionerLogicalId).get("name").replace("''", "'"));
                            patient.setIdentifiers(new HashSet<>(identifierRepository.findByUser(user)));
                            gpPatients.add(patient);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("SQL exception:", e);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection:", e2);
            }
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return gpPatients;
    }

    public String getLocationUuidFromLogicalUuid(UUID logicalId, String tableName) throws FhirResourceException {
        String output = null;

        StringBuilder query = new StringBuilder();
        query.append("SELECT content->>'location' ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE logical_id = '");
        query.append(logicalId.toString());
        query.append("' ");

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            while ((results.next())) {
                output = results.getString(1);
            }

        } catch (SQLException e) {
            LOG.error("Unable to get location uuids by logical id {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }

        return output;
    }

    @Transactional(readOnly = true)
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
        java.sql.Statement statement = null;
        ResultSet results = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public List<UUID> getLogicalIdsBySubjectIdAndIdentifierValue(
            final String tableName, final UUID subjectId, final String identifierValue)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM ");
        query.append(tableName);
        query.append(" WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' AND CONTENT #> '{identifier,0}' ->> 'value' ='");
        query.append(identifierValue);
        query.append("' ");

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
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
                query.append("'").append(namesToIgnore.get(i).toUpperCase()).append("'");

                if (i != (namesToIgnore.size() - 1)) {
                    query.append(",");
                }
            }

            query.append(") ");
        }

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
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
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
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
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public List<Observation> getObservationsBySubjectAndName(UUID subjectId, List<String> names)
            throws FhirResourceException{
        StringBuilder nameString = new StringBuilder();
        int count = 0;

        for (String name : names) {
            nameString.append("'").append(name).append("'");
            if (count < names.size() - 1) {
                nameString.append(",");
            }
            count++;
        }

        String query = "SELECT content::varchar FROM observation " +
                "WHERE content -> 'subject' ->> 'display' = '" +  subjectId.toString() + "' " +
                "AND UPPER(content-> 'name' ->> 'text') IN (" + nameString.toString() + ") ";

        return findResourceByQuery(query, Observation.class);
    }

    @Transactional(readOnly = true)
    public List<UUID> getObservationUuidsBySubjectNameDateRange(UUID subjectId, String name, Date start, Date end)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content->'appliesDateTime' ");
        query.append("FROM observation ");
        query.append("WHERE content -> 'subject' ->> 'display' = '");
        query.append(subjectId);
        query.append("' ");
        query.append("AND UPPER(content-> 'name' ->> 'text') = '");
        query.append(name.toUpperCase());
        query.append("' ");

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return map of logical ids
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<UUID> observationUuids = new ArrayList<>();

            while ((results.next())) {
                if (StringUtils.isNotEmpty(results.getString(2))) {
                    try {
                        String dateString = results.getString(2).replace("\"", "");
                        XMLGregorianCalendar xmlDate
                                = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                        Long applies = xmlDate.toGregorianCalendar().getTime().getTime();

                        if (start.getTime() <= applies && end.getTime() >= applies) {
                            observationUuids.add(UUID.fromString(results.getString(1)));
                        }
                    } catch (DatatypeConfigurationException e) {
                        LOG.error(e.getMessage());
                    }
                }
            }

            return observationUuids;
        } catch (SQLException e) {
            LOG.error("Unable to get logical ids by subject id, date range {}", e);

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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    public List<Procedure> getProceduresByEncounter(UUID encounterId) throws FhirResourceException{
        String query = "SELECT content::varchar FROM procedure " +
                "WHERE CONTENT -> 'encounter' ->> 'display' = '" + encounterId.toString() + "'";

        return findResourceByQuery(query, Procedure.class);
    }

    public JSONObject getResource(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        LOG.debug("Getting resource {}", uuid.toString());
        PGobject result;
        Connection connection = null;
        CallableStatement proc = null;
        try {
            connection = dataSource.getConnection();
            proc = connection.prepareCall("{call fhir_read( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();
            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());

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
        } finally {
            try {
                if (proc != null) {
                    proc.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close CallableStatement {}", e2);
            }

            DbUtils.closeQuietly(connection);
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

    public List<UUID> getUuidByQuery(final String query) throws FhirResourceException {
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query);
            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }
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
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
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

    /**
     *
     * FUNCTION fhir_update(cfg jsonb, _type varchar, id uuid, vid uuid, resource jsonb, tags jsonb)
     *
     */
    @Deprecated
    public UUID update(Resource resource, FhirLink fhirLink) throws FhirResourceException {

        PGobject result;
        Connection connection = null;
        CallableStatement proc = null;
        try {
            connection = dataSource.getConnection();
            proc = connection.prepareCall("{call fhir_update( ?::jsonb, ?, ?, ?,  ?::jsonb, ?::jsonb)}");
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
        } finally {
            try {
                if (proc != null) {
                    proc.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close CallableStatement {}", e2);
            }

            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Natively update FHIR entity, returning version UUID.
     * @param resource Resource to update, e.g. Observation, Patient, etc
     * @param resourceType Type of the Resource, e.g. "Observation", "Patient" etc
     * @param logicalId Logical UUID of the object to update, the primary key
     * @return FhirDatabaseEntity that has just been stored, including version, logical ids etc
     * @throws FhirResourceException
     */
    public FhirDatabaseEntity updateEntity(Resource resource, String resourceType, String tableName, UUID logicalId)
            throws FhirResourceException {
        FhirDatabaseEntity entity = new FhirDatabaseEntity(marshallFhirRecord(resource), resourceType);

        executeSQL("UPDATE " + tableName + " SET content = '" + CommonUtils.cleanSql(entity.getContent()) +
                "', version_id = '" + entity.getVersionId() +
                "', updated = '" + entity.getUpdated() +
                "' WHERE logical_id = '" + logicalId.toString() + "' ");

        return entity;
    }
}
