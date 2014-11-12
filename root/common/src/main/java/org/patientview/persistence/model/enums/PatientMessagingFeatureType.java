package org.patientview.persistence.model.enums;

/**
 * Restricted list of features used when searching for conversation recipients by patients
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum PatientMessagingFeatureType {

    // 12/11/14 restricted to just default messaging contact
    //UNIT_TECHNICAL_CONTACT("Unit Technical Contact"),
    //PATIENT_SUPPORT_CONTACT("Patient Support Contact"),
    DEFAULT_MESSAGING_CONTACT("Default Messaging Contact");

    private String name;
    PatientMessagingFeatureType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
