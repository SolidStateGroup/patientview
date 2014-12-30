package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
public class AlertObservationHeading {

    private Long id;
    private boolean webAlert;
    private boolean webAlertViewed;
    private boolean emailAlert;
    private boolean emailAlertSent;
    private BaseUser user;
    private ObservationHeading observationHeading;

    public AlertObservationHeading(org.patientview.persistence.model.AlertObservationHeading alertObservationHeading) {
        this.id = alertObservationHeading.getId();
        this.webAlert = alertObservationHeading.isWebAlert();
        this.webAlertViewed = alertObservationHeading.isWebAlertViewed();
        this.emailAlert = alertObservationHeading.isEmailAlert();
        this.emailAlertSent = alertObservationHeading.isEmailAlertSent();
        this.user = new BaseUser(alertObservationHeading.getUser());
        this.observationHeading = new ObservationHeading(alertObservationHeading.getObservationHeading());
    }

    public AlertObservationHeading() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
