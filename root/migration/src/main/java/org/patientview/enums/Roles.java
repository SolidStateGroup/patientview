package org.patientview.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Roles {
 /*   PATIENT("Patient"), UNIT_ADMIN("Unit Admin"),
    STAFF_ADMIN("Staff Admin"), SPECIALTY_ADMIN("Specialty Admin"),
    GLOBAL_ADMIN("Global Admin"), GP_ADMIN("GP Admin"),
    MEMBER("Member");

    private String name;
    Roles(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); } */

    PATIENT, UNIT_ADMIN,
    STAFF_ADMIN, SPECIALTY_ADMIN,
    GLOBAL_ADMIN, GP_ADMIN,
    MEMBER;


}
