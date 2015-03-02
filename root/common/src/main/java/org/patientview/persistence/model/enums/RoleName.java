package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum RoleName {

    PATIENT("Patient"),
    UNIT_ADMIN("Unit Admin"),
    STAFF_ADMIN("Unit Staff"),
    DISEASE_GROUP_ADMIN("Disease Group Admin"),
    SPECIALTY_ADMIN("Specialty Admin"),
    GLOBAL_ADMIN("Global Admin"),
    GP_ADMIN("GP Admin"),
    MEMBER("Member"),
    PUBLIC("Public"),
    UNIT_ADMIN_API("Unit Admin API");

    private String name;
    RoleName(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
