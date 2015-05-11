package org.patientview.persistence.model;

import org.apache.commons.lang.NullArgumentException;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/04/2015
 * Used to interact directly with FHIR organization table. When creating a new entity must also generate logical Id and
 * set published date to same as updated.
 */
public class FhirDatabaseEntity {

    private UUID logicalId;
    private UUID versionId;
    private String resourceType;
    private Date updated;
    private Date published;
    private String category = null;
    private String content;

    public FhirDatabaseEntity(String content, String resourceType) throws NullArgumentException {
        this.versionId = UUID.randomUUID();
        this.resourceType = resourceType;

        // as creating, set both published and updated to same time now
        Date now = new Date();
        this.updated = now;

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
        if (versionId == null) {
            versionId = UUID.randomUUID();
        }
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
