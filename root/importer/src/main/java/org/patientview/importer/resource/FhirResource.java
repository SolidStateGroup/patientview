package org.patientview.importer.resource;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.importer.exception.FhirResourceException;
import org.patientview.importer.util.Util;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
@Component
public class FhirResource {

    private final Logger LOG = LoggerFactory.getLogger(FhirResource.class);
    private static final String  config = "\"{\"base\":\"http:/myserver\"}";
    private static final JsonParser jsonParser = new JsonParser();

    private DriverManagerDataSource dataSource;

    {
        // TODO connection pool
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://127.0.0.1:5432/fhir");
        dataSource.setUsername("fhir");
        dataSource.setPassword("fhir");
    }


    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource
     * @return
     * @throws SQLException
     * @throws FhirResourceException
     */
    public UUID createResource(Resource resource) throws FhirResourceException {

        PGobject result;
        try {
            CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, "{\"base\":\"http:/myserver\"}");
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, Util.marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            JSONArray patientResult = (JSONArray) jsonObject.get("entry");
            JSONObject patient = (JSONObject) patientResult.get(0);
            proc.close();
            return UUID.fromString(patient.get("id").toString());

        } catch (SQLException e) {
            LOG.error("Unable to create resource {}", e);
            throw new FhirResourceException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return null;
        } catch (Exception e) {
            LOG.error("Could not parse resource", e);
            throw new FhirResourceException(e.getMessage());
        }


    }

    /**
     * For FUNCTION fhir_delete(cfg jsonb, _type varchar, id uuid)
     *
     */
    public void delete(UUID uuid, ResourceType resourceType) throws SQLException, FhirResourceException {

        LOG.debug("Delete resource {}", uuid.toString());
        CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_delete( ?::jsonb, ?, ?)}");
        proc.setObject(1, "{\"base\":\"http:/myserver\"}");
        proc.setObject(2, resourceType.name());
        proc.setObject(3, uuid);
        proc.execute();
    }

}
