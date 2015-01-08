package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
@Entity
@Table(name = "pv_alert_observation_heading")
public class AlertObservationHeading extends AuditModel {

    @Column(name = "web_alert", nullable = false)
    private boolean webAlert;

    @Column(name = "web_alert_viewed", nullable = false)
    private boolean webAlertViewed;

    @Column(name = "email_alert", nullable = false)
    private boolean emailAlert;

    @Column(name = "email_alert_sent", nullable = false)
    private boolean emailAlertSent;

    @Column(name = "latest_observation_value")
    private String latestObservationValue;

    @Column(name = "latest_observation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestObservationDate;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "observation_heading_id", nullable = false)
    private ObservationHeading observationHeading;

    // used during import
    @Transient
    private boolean updated;

    public boolean isWebAlert() {
        return webAlert;
    }

    public void setWebAlert(boolean webAlert) {
        this.webAlert = webAlert;
    }

    public boolean isWebAlertViewed() {
        return webAlertViewed;
    }

    public void setWebAlertViewed(boolean webAlertViewed) {
        this.webAlertViewed = webAlertViewed;
    }

    public boolean isEmailAlert() {
        return emailAlert;
    }

    public void setEmailAlert(boolean emailAlert) {
        this.emailAlert = emailAlert;
    }

    public boolean isEmailAlertSent() {
        return emailAlertSent;
    }

    public void setEmailAlertSent(boolean emailAlertSent) {
        this.emailAlertSent = emailAlertSent;
    }

    public String getLatestObservationValue() {
        return latestObservationValue;
    }

    public void setLatestObservationValue(String latestObservationValue) {
        this.latestObservationValue = latestObservationValue;
    }

    public Date getLatestObservationDate() {
        return latestObservationDate;
    }

    public void setLatestObservationDate(Date latestObservationDate) {
        this.latestObservationDate = latestObservationDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
