package org.patientview.api.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Representation of MedlinePlus response json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedlineplusResponseJson {

    private ResponseFeed feed;

    @JsonIgnore
    public void parse(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        MedlineplusResponseJson prototype = mapper.readValue(body, MedlineplusResponseJson.class);
        fromPrototype(prototype);
    }

    @JsonIgnore
    private void fromPrototype(MedlineplusResponseJson prototype) throws IOException {
        setFeed(prototype.getFeed());
    }

    public ResponseFeed getFeed() {
        return feed;
    }

    public void setFeed(ResponseFeed feed) {
        this.feed = feed;
    }

    public class ResponseFeed {
        private String xsi;
        private String base;
        private String lang;
        private ValueTypeJson title;
        private ValueTypeJson subtitle;
        @JsonIgnore
        private String author;
        private ValueTypeJson updated;
        @JsonIgnore
        private String[] category;
        @JsonIgnore
        private ValueTypeJson id;
        private EntryJson[] entry;

        public String getXsi() {
            return xsi;
        }

        public void setXsi(String xsi) {
            this.xsi = xsi;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public ValueTypeJson getTitle() {
            return title;
        }

        public void setTitle(ValueTypeJson title) {
            this.title = title;
        }

        public ValueTypeJson getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(ValueTypeJson subtitle) {
            this.subtitle = subtitle;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public ValueTypeJson getUpdated() {
            return updated;
        }

        public void setUpdated(ValueTypeJson updated) {
            this.updated = updated;
        }

        public String[] getCategory() {
            return category;
        }

        public void setCategory(String[] category) {
            this.category = category;
        }

        public ValueTypeJson getId() {
            return id;
        }

        public void setId(ValueTypeJson id) {
            this.id = id;
        }

        public EntryJson[] getEntry() {
            return entry;
        }

        public void setEntry(EntryJson[] entry) {
            this.entry = entry;
        }
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
