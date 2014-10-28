package org.patientview.persistence.resource;

import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.config.exception.FhirResourceException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
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

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource Resource to create
     * @return JSONObject JSON version of saved Resource
     * @throws FhirResourceException
     */
    public JSONObject create(Resource resource) throws FhirResourceException {

        PGobject result;
        Connection connection;
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
            connection.close();
            return new JSONObject(result.getValue());

        } catch (SQLException e) {
            LOG.error("Unable to build resource {}", e);
            throw new FhirResourceException(e.getMessage());
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }

    }

    public JSONObject getResource(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        LOG.debug("Getting resource {}", uuid.toString());
        PGobject result;
        try {
            Connection connection = dataSource.getConnection();
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

    public <T extends Resource> Object getResourceConverted(UUID uuid, ResourceType resourceType)
            throws FhirResourceException{
        JSONObject object = getResource(uuid, resourceType);
        try {
            return (T) jsonParser.parse(new ByteArrayInputStream(object.toString().getBytes()));
        } catch (Exception e) {
            throw new FhirResourceException("Cannot convert resource");
        }
    }

    public <T extends Resource> List<T> findResourceByQuery(String sql, Class<T> resourceType) throws FhirResourceException {
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);
            List<T> resultsList = convertResultSet(results);
            connection.close();
            return resultsList;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
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
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            List<String[]> observations = new ArrayList<>();

            while ((results.next())) {
                String[] res = {results.getString(1), results.getString(2), results.getString(3)};
                observations.add(res);
            }

            connection.close();
            return observations;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }

    private String marshallFhirRecord(Resource resource) throws FhirResourceException {
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
}
