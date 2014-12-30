package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
public class AlertObservationHeading {

    private boolean webAlert;
    private boolean webAlertViewed;
    private boolean emailAlert;
    private boolean emailAlertSent;
    private ObservationHeading observationHeading;

    public AlertObservationHeading(org.patientview.persistence.model.AlertObservationHeading alertObservationHeading) {
        this.webAlert = alertObservationHeading.isWebAlert();
        this.webAlertViewed = alertObservationHeading.isWebAlertViewed();
        this.emailAlert = alertObservationHeading.isEmailAlert();
        this.emailAlertSent = alertObservationHeading.isEmailAlertSent();
        this.observationHeading = new ObservationHeading(alertObservationHeading.getObservationHeading());
    }

    public AlertObservationHeading() {

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

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }
}
