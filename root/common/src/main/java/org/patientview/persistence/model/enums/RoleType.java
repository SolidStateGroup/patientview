package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 20/07/2014
 */
public enum RoleType {

    PATIENT("Patient"), STAFF("Staff");

    private String name;
    RoleType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }


}
