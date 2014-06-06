package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@MappedSuperclass
public class SimpleAuditModel extends BaseModel {

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

    protected SimpleAuditModel() {

    }

    @JsonIgnore
    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    public void setCreator(final User creator) {
        this.creator = creator;
    }
}

