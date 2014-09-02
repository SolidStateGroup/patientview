package org.patientview.persistence;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.persistence.exception.FhirResourceException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public class FhirResource {

    private final Logger LOG = LoggerFactory.getLogger(FhirResource.class);
    private static final String  config =  "{\"base\":\"http:/myserver\"}";
    private static final JsonParser jsonParser = new JsonParser();

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    private JSONObject getResource(UUID uuid, ResourceType resourceType) throws FhirResourceException {
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

}
