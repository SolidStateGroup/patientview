package org.patientview.persistence.model.enums;

/**
 * Restricted list of features used when searching for conversation recipients by staff
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum StaffMessagingFeatureType {

    MESSAGING("Messaging"),
    DEFAULT_MESSAGING_CONTACT("Default Messaging Contact");

    private String name;
    StaffMessagingFeatureType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
