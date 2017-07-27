package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.AlertTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
 * Created on 14/01/2015
 */
@Entity
@Table(name = "pv_alert")
public class Alert extends AuditModel {

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertTypes alertType;

    @Column(name = "web_alert", nullable = false)
    private boolean webAlert;

    @Column(name = "web_alert_viewed", nullable = false)
    private boolean webAlertViewed;

    @Column(name = "email_alert", nullable = false)
    private boolean emailAlert;

    @Column(name = "email_alert_sent", nullable = false)
    private boolean emailAlertSent;

    @Column(name = "mobile_alert", nullable = false)
    private boolean mobileAlert;

    @Column(name = "mobile_alert_sent", nullable = false)
    private boolean mobileAlertSent;

    @Column(name = "latest_value")
    private String latestValue;

    @Column(name = "latest_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestDate;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // only for result alerts
    @OneToOne
    @JoinColumn(name = "observation_heading_id")
    private ObservationHeading observationHeading;

    // used during import
    @Transient
    private boolean updated;

    public AlertTypes getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertTypes alertType) {
        this.alertType = alertType;
    }

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

    public boolean isMobileAlert() {
        return mobileAlert;
    }

    public void setMobileAlert(boolean mobileAlert) {
        this.mobileAlert = mobileAlert;
    }

    public boolean isMobileAlertSent() {
        return mobileAlertSent;
    }

    public void setMobileAlertSent(boolean mobileAlertSent) {
        this.mobileAlertSent = mobileAlertSent;
    }

    public String getLatestValue() {
        return latestValue;
    }

    public void setLatestValue(String latestValue) {
        this.latestValue = latestValue;
    }

    public Date getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(Date latestDate) {
        this.latestDate = latestDate;
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
