package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 */
@Entity
@Table(name = "pv_feature_user")
public class UserFeature extends RangeModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(name = "opt_in_status")
    private Boolean optInStatus;

    @Column(name = "opt_in_hidden")
    private Boolean optInHidden;

    @Column(name = "opt_out_hidden")
    private Boolean optOutHidden;

    @Column(name = "opt_in_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date optInDate;

    public UserFeature () {
    }

    // used by migration
    public UserFeature (Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(final Feature feature) {
        this.feature = feature;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Boolean getOptInStatus() {
        return optInStatus;
    }

    public void setOptInStatus(Boolean optInStatus) {
        this.optInStatus = optInStatus;
    }

    public Boolean getOptInHidden() {
        return optInHidden;
    }

    public void setOptInHidden(Boolean optInHidden) {
        this.optInHidden = optInHidden;
    }

    public Boolean getOptOutHidden() {
        return optOutHidden;
    }

    public void setOptOutHidden(Boolean optOutHidden) {
        this.optOutHidden = optOutHidden;
    }

    public Date getOptInDate() {
        return optInDate;
    }

    public void setOptInDate(Date optInDate) {
        this.optInDate = optInDate;
    }
}
