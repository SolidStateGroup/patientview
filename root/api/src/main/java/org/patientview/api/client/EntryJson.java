package org.patientview.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representation of Entry as part of the MedlinePlus response json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntryJson {
    private ValueTypeJson title;
    private LinkJson[] link;
    private ValueTypeJson id;
    private ValueTypeJson updated;
    private ValueTypeJson summary;


    public ValueTypeJson getTitle() {
        return title;
    }

    public void setTitle(ValueTypeJson title) {
        this.title = title;
    }

    public LinkJson[] getLink() {
        return link;
    }

    public void setLink(LinkJson[] link) {
        this.link = link;
    }

    public ValueTypeJson getId() {
        return id;
    }

    public void setId(ValueTypeJson id) {
        this.id = id;
    }

    public ValueTypeJson getUpdated() {
        return updated;
    }

    public void setUpdated(ValueTypeJson updated) {
        this.updated = updated;
    }

    public ValueTypeJson getSummary() {
        return summary;
    }

    public void setSummary(ValueTypeJson summary) {
        this.summary = summary;
    }

}
