package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
    @Column(name = "action")
    private AuditActions auditActions;

    @Column(name = "source_object_id")
    private Long sourceObjectId;

    @Column(name = "source_object_type")
    @Enumerated(EnumType.STRING)
    private AuditObjectTypes sourceObjectType;

    @Column(name = "pre_value")
    private String preValue;

    @Column(name = "post_value")
    private String postValue;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    // used when auditing importer
    @Column(name = "identifier")
    private String identifier;

    // used when auditing importer
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    // used when auditing importer
    @Column(name = "information")
    private String information;

    // used when auditing importer
    @Column(name = "xml")
    private String xml;

    public AuditActions getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(final AuditActions auditActions) {
        this.auditActions = auditActions;
    }

    public Long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final Long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public AuditObjectTypes getSourceObjectType() {
        return sourceObjectType;
    }

    public void setSourceObjectType(final AuditObjectTypes sourceObjectType) {
        this.sourceObjectType = sourceObjectType;
    }

    // required for aspect
    public void setSourceObjectType(final String sourceObjectType) {
        for (AuditObjectTypes auditObjectType : AuditObjectTypes.class.getEnumConstants()) {
            if (auditObjectType.getName().equals(sourceObjectType)) {
                this.setSourceObjectType(auditObjectType);
            }
        }
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
