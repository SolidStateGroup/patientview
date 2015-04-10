package org.patientview.api.model;

import org.patientview.persistence.model.enums.FeatureType;

/**
 * Contact Alert, used to show staff if contact information is missing (no user with Feature) for a Group,
 * e.g. DEFAULT_MESSAGING_CONTACT
 * Created by jamesr@solidstategroup.com
 * Created on 02/04/2015
 */
public class ContactAlert {

    private BaseGroup group;
    private String featureName;

    public ContactAlert(org.patientview.persistence.model.Group group, FeatureType featureType) {
        this.group = new BaseGroup(group);
        this.featureName = featureType.getName();
    }

    public ContactAlert(org.patientview.api.model.Group group, FeatureType featureType) {
        this.group = new BaseGroup(group);
        this.featureName = featureType.getName();
    }

    public BaseGroup getGroup() {
        return group;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
}
