package org.patientview.api.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/10/2014
 *
 * Used for shared group codes, e.g. for storing patient entered data
 */
public enum GroupCode {

    PATIENT_ENTERED("Patient Entered Data");

    private String name;
    GroupCode(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
