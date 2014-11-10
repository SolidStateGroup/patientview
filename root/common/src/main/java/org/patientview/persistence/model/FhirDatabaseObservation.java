package org.patientview.persistence.model;

import org.apache.commons.lang.NullArgumentException;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 * Used to interact directly with FHIR observation table (not recommended but needed for migration speed)
 */
public class FhirDatabaseObservation {

    private UUID logicalId;
    private UUID versionId;
    private String resourceType;
    private Date updated;
    private Date published;
    private String category = null;
    private String content;

    public FhirDatabaseObservation(String content) throws NullArgumentException {

        // new Observation so generate UUIDs for logical and version Ids
        this.logicalId = UUID.randomUUID();
        this.versionId = UUID.randomUUID();

        // Type will always be observation
        this.resourceType = "Observation";

        // as creating, set both published and updated to same time now
        Date now = new Date();
        this.updated = now;
        this.published = now;

        // set json string as content, throwing exception if null
        if (content == null) {
            throw new NullArgumentException("content cannot be null");
        }

        this.content = content;
    }

    public UUID getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(UUID logicalId) {
        this.logicalId = logicalId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public void setVersionId(UUID versionId) {
        this.versionId = versionId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
