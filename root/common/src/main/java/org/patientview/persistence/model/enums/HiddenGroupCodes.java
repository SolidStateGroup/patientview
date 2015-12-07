package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/10/2014
 *
 * Used for shared group codes, e.g. for storing patient entered data
 */
public enum HiddenGroupCodes {

    PATIENT_ENTERED("Patient Entered Data"),
    STAFF_ENTERED("Staff Entered Data"),
    ECS("ECS");

    private String name;
    HiddenGroupCodes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
