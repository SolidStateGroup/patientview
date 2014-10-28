package org.patientview;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * used by gson to generate correctly formatted dates
 */
public class DateSerializer implements JsonSerializer<Date> {
    public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return new JsonPrimitive(sdf.format(date));
    }
}
