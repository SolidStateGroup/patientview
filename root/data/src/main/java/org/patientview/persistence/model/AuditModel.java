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
public class AuditModel extends SimpleAuditModel {

    @Column(name = "last_update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdater;

    protected AuditModel() {

    }

    @JsonIgnore
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonIgnore
    public User getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(final User lastUpdater) {
        this.lastUpdater = lastUpdater;
    }
}
