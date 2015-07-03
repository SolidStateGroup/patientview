package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/06/2015
 */
public enum PractitionerRoles {
    GP("GP"),
    IBD_NURSE("IBD Nurse"),
    NAMED_CONSULTANT("Named Consultant");

    private String name;
    PractitionerRoles(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
