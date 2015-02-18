package org.patientview.api.model;

import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.FeatureType;

/**
 * GpMedicationStatus, representing the availability of GP medication and the User's opt-in/out status.
 * Created by jamesr@solidstategroup.com
 * Created on 24/11/2014
 */
public class GpMedicationStatus {

    // from UserFeature
    private boolean optInStatus;
    private boolean optInHidden;
    private boolean optOutHidden;
    private Long optInDate;

    // from GroupFeature (if group has GP_MEDICATION feature)
    private boolean available;

    public GpMedicationStatus() {
    }

    public GpMedicationStatus(UserFeature userGpMedicationFeature) {
        if (userGpMedicationFeature.getFeature().getName().equals(FeatureType.GP_MEDICATION.toString())) {
            if (userGpMedicationFeature.getOptInStatus() != null) {
                this.optInStatus = userGpMedicationFeature.getOptInStatus();
            }
            if (userGpMedicationFeature.getOptInHidden() != null) {
                this.optInHidden = userGpMedicationFeature.getOptInHidden();
            }
            if (userGpMedicationFeature.getOptOutHidden() != null) {
                this.optOutHidden = userGpMedicationFeature.getOptOutHidden();
            }
            if (userGpMedicationFeature.getOptInDate() != null) {
                this.optInDate = userGpMedicationFeature.getOptInDate().getTime();
            }
        }
    }

    public boolean getOptInStatus() {
        return optInStatus;
    }

    public void setOptInStatus(boolean optInStatus) {
        this.optInStatus = optInStatus;
    }

    public boolean getOptInHidden() {
        return optInHidden;
    }

    public void setOptInHidden(boolean optInHidden) {
        this.optInHidden = optInHidden;
    }

    public boolean getOptOutHidden() {
        return optOutHidden;
    }

    public void setOptOutHidden(boolean optOutHidden) {
        this.optOutHidden = optOutHidden;
    }

    public Long getOptInDate() {
        return optInDate;
    }

    public void setOptInDate(Long optInDate) {
        this.optInDate = optInDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
