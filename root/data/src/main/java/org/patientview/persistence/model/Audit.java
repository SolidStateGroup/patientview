package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.AuditActions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
@Entity
@Table(name = "pv_audit")
public class Audit extends BaseModel {

    @Enumerated(EnumType.STRING)
    private AuditActions auditActions;

    @Column(name = "source_object_type")
    private String source;

    @Column(name = "source_object_id")
    private Long sourceObjectId;

    @Column(name = "source_object_type")
    private String sourceObjectType;

    @Column(name = "pre_value")
    private String preValue;

    @Column(name = "post_value")
    private String postValue;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    public AuditActions getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(final AuditActions auditActions) {
        this.auditActions = auditActions;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public Long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final Long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public String getSourceObjectType() {
        return sourceObjectType;
    }

    public void setSourceObjectType(final String sourceObjectType) {
        this.sourceObjectType = sourceObjectType;
    }

    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(final String preValue) {
        this.preValue = preValue;
    }

    public String getPostValue() {
        return postValue;
    }

    public void setPostValue(final String postValue) {
        this.postValue = postValue;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(final Long actorId) {
        this.actorId = actorId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }
}
