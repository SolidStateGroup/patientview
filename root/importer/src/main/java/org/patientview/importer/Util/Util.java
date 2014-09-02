package org.patientview.importer.util;

import generated.Patientview;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.importer.exception.ImportResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class Util {


    private final static Logger LOG = LoggerFactory.getLogger(Util.class);
    private static JsonParser jsonParser;

    static {
        jsonParser = new JsonParser();
    }

    public static StringWriter marshallPatientRecord(Patientview patientview) throws ImportResourceException {

        StringWriter stringWriter = null;

        try {
            JAXBContext context = JAXBContext.newInstance(Patientview.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientview, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

        return stringWriter;

    }

    public static Patientview unmarshallPatientRecord(String content) throws ImportResourceException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Patientview.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Patientview) unmarshaller.unmarshal(new StringReader(content));
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

    }

    public static String marshallFhirRecord(Resource resource) throws FhirResourceException {
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

    public static UUID getResourceId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.get("id").toString());

    }

    public static Resource getResource(final JSONObject bundle) throws Exception {
        JSONArray jsonArray  = (JSONArray) bundle.get("entry");
        JSONObject element = (JSONObject) jsonArray.get(0);

        return jsonParser.parse(new ByteArrayInputStream(element.get("content").toString().getBytes()));
    }

}
