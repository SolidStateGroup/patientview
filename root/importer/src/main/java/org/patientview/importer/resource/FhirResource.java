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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
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
    private static final String  config =  "{\"base\":\"http:/myserver\"}";
    private static final JsonParser jsonParser = new JsonParser();

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    /**
     * For FUNCTION fhir_create(cfg jsonb, _type varchar, resource jsonb, tags jsonb)
     *
     * @param resource
     * @return
     * @throws SQLException
     * @throws FhirResourceException
     */
    public JSONObject create(Resource resource) throws FhirResourceException {

        PGobject result;
        try {
            CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_create( ?::jsonb, ?, ?::jsonb, ?::jsonb)}");
            proc.setObject(1, config);
            proc.setObject(2, resource.getResourceType().name());
            proc.setObject(3, Util.marshallFhirRecord(resource));
            proc.setObject(4, null);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();

            result = (PGobject) proc.getObject(1);
            return new JSONObject(result.getValue());

        } catch (SQLException e) {
            LOG.error("Unable to create resource {}", e);
            throw new FhirResourceException(e.getMessage());
        }

    }


    /**
     *
     * FUNCTION fhir_update(cfg jsonb, _type varchar, id uuid, vid uuid, resource jsonb, tags jsonb)
     *
     */
    public UUID update(Resource resource, UUID resourceId, UUID versionId) throws FhirResourceException {

        PGobject result;
        try {
            CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_update( ?::jsonb, ?, ?, ?,  ?::jsonb, ?::jsonb)}");
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
            return Util.getVersionId(jsonObject);

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
        CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_delete( ?::jsonb, ?, ?)}");
        proc.setObject(1, config);
        proc.setObject(2, resourceType.name());
        proc.setObject(3, uuid);
        proc.execute();
    }


    private JSONObject getBundle(UUID uuid, ResourceType resourceType) throws FhirResourceException {
        LOG.debug("Getting resource {}", uuid.toString());
        PGobject result;
        try {
            CallableStatement proc = dataSource.getConnection().prepareCall("{call fhir_read( ?::jsonb, ?, ?)}");
            proc.setObject(1, config);
            proc.setObject(2, resourceType.name());
            proc.setObject(3, uuid);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();
            result = (PGobject) proc.getObject(1);
            JSONObject jsonObject = new JSONObject(result.getValue());
            proc.close();
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
            return jsonParser.parse(new ByteArrayInputStream(resource.toString().getBytes()));
        } catch (Exception e) {
            throw new FhirResourceException(e.getMessage());
        }
    }

}
