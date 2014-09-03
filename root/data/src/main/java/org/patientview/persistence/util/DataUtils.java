package org.patientview.persistence.util;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public class DataUtils {

    private final static Logger LOG = LoggerFactory.getLogger(DataUtils.class);
    private static JsonParser jsonParser;

    static {
        jsonParser = new JsonParser();
    }

    public static Resource getResource(final JSONObject bundle) throws Exception {
        JSONArray jsonArray  = (JSONArray) bundle.get("entry");
        JSONObject element = (JSONObject) jsonArray.get(0);

        return jsonParser.parse(new ByteArrayInputStream(element.get("content").toString().getBytes()));
    }

}
