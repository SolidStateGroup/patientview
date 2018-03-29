package org.patientview.api.client.nhschoices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Representation of NHSChoices response json model for API v2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NhsChoicesResponseJson {

    @JsonProperty("significantLink")
    private List<ConditionLinkJson> conditionLinks;

    // extract these from nested objects
    private String organisationId;
    private String organisationPhone;
    private String organisationUrl;


    @SuppressWarnings("unchecked")
    @JsonProperty("Organisation")
    private void unpackOrganisation(Map<String, Object> org) {
        this.organisationId = (String) org.get("OrganisationId");
        this.organisationPhone = (String) org.get("Telephone");
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("feed")
    private void unpackFeed(Map<String, Object> feed) {
        List<Map<String, Object>> links = (List<Map<String, Object>>) feed.get("link");
        if (links != null && !links.isEmpty()) {
            // 2 types of links "self" and "alternate", we interested in "alternate"
            for (Map<String, Object> link : links) {
                String rel = (String) link.get("@rel");
                String href = (String) link.get("@href");
                if (StringUtils.isNoneBlank(rel) && StringUtils.isNoneBlank(href) &&
                        rel.equals("alternate")) {
                    this.organisationUrl = href;
                    break;
                }
            }
        }
    }

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
        setOrganisationId(prototype.getOrganisationId());
        setOrganisationPhone(prototype.getOrganisationPhone());
        setOrganisationUrl(prototype.getOrganisationUrl());
    }

    public List<ConditionLinkJson> getConditionLinks() {
        return conditionLinks;
    }

    public void setConditionLinks(List<ConditionLinkJson> conditionLinks) {
        this.conditionLinks = conditionLinks;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationPhone() {
        return organisationPhone;
    }

    public void setOrganisationPhone(String organisationPhone) {
        this.organisationPhone = organisationPhone;
    }

    public String getOrganisationUrl() {
        return organisationUrl;
    }

    public void setOrganisationUrl(String organisationUrl) {
        this.organisationUrl = organisationUrl;
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
