package org.patientview;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * used by gson to parse epoch dates
 */
public class DateDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (StringUtils.isNotEmpty(json.getAsJsonPrimitive().getAsString())) {
            return new Date(Long.parseLong(json.getAsJsonPrimitive().getAsString()));
        }
        return null;
    }
}
