package org.patientview.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic representation of different fields in MedlinePlus response json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueTypeJson {

    @JsonProperty("_value")
    private String value;
    private String type;

    public String getValue() {
        return value;
    }

    public void setValue(String _value) {
        this.value = _value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
