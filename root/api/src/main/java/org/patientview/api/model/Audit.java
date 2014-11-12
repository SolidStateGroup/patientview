package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
public class Audit extends BaseModel {

    private String auditActions;
    private Long sourceObjectId;
    private String sourceObjectType;
    private String preValue;
    private String postValue;
    private Long actorId;
    private Date creationDate;

    // new for transport object
    private User actor;
    private User sourceObjectUser;

    public Audit() {

    }

    public Audit(org.patientview.persistence.model.Audit audit) {
        if (audit.getAuditActions() != null) {
            this.auditActions = audit.getAuditActions().getName();
        }
        this.sourceObjectId = audit.getSourceObjectId();
        this.sourceObjectType = audit.getSourceObjectType();
        this.preValue = audit.getPreValue();
        this.postValue = audit.getPostValue();
        this.actorId = audit.getActorId();
        this.creationDate = audit.getCreationDate();
    }

    public String getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(final String auditActions) {
        this.auditActions = auditActions;
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

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public User getSourceObjectUser() {
        return sourceObjectUser;
    }

    public void setSourceObjectUser(User sourceObjectUser) {
        this.sourceObjectUser = sourceObjectUser;
    }
}
