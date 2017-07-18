package org.patientview.api.model;

import org.patientview.persistence.model.enums.AlertTypes;

import java.util.Date;

/**
 * Alert, used to represent result or letter alerts created by a User to inform them of updated results or letters when
 * new data is imported from their Groups.
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
public class Alert {

    private Long id;
    private AlertTypes alertType;
    private boolean webAlert;
    private boolean webAlertViewed;
    private boolean emailAlert;
    private boolean emailAlertSent;
    private boolean mobileAlert;
    private boolean mobileAlertSent;
    private String latestValue;
    private Date latestDate;
    private BaseUser user;
    private ObservationHeading observationHeading;

    public Alert() {
    }

    public Alert(org.patientview.persistence.model.Alert alert, org.patientview.persistence.model.User user) {
        this.id = alert.getId();
        this.alertType = alert.getAlertType();
        this.webAlert = alert.isWebAlert();
        this.webAlertViewed = alert.isWebAlertViewed();
        this.emailAlert = alert.isEmailAlert();
        this.emailAlertSent = alert.isEmailAlertSent();
        this.mobileAlert = alert.isMobileAlert();
        this.mobileAlertSent = alert.isMobileAlertSent();
        this.latestValue = alert.getLatestValue();
        this.latestDate = alert.getLatestDate();
        this.user = new BaseUser(user);
        if (alert.getObservationHeading() != null) {
            this.observationHeading = new ObservationHeading(alert.getObservationHeading());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
        this.user = user;
    }

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }
}
