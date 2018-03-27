package org.patientview.api.client.nhschoices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Representation of NHSChoices response json model for API v2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NhsChoicesResponseJson {

    @JsonProperty("significantLink")
    private ConditionLink[] conditionLinks;

    @JsonIgnore
    public void parse(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        NhsChoicesResponseJson prototype = mapper.readValue(body, NhsChoicesResponseJson.class);
        fromPrototype(prototype);
    }

    @JsonIgnore
    private void fromPrototype(NhsChoicesResponseJson prototype) throws IOException {
        setConditionLinks(prototype.getConditionLinks());
    }

    public ConditionLink[] getConditionLinks() {
        return conditionLinks;
    }

    public void setConditionLinks(ConditionLink[] conditionLinks) {
        this.conditionLinks = conditionLinks;
    }

    @JsonIgnore
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }
}
