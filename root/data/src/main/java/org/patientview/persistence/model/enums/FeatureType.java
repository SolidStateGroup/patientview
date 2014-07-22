package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum FeatureType {

    SHARING_THOUGHTS("Sharing Thoughts"), MESSAGING("Messaging"), FEEDBACK("Feedback"), ECS("Emergency Care Summary");

    private String name;
    FeatureType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
