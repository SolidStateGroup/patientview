package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 27/08/2014
 */
@Entity
@Table(name = "pv_fhir_link")
public class FhirLink extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "identifier_id")
    private Identifier identifier;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final Identifier identifier) {
        this.identifier = identifier;
    }
}
