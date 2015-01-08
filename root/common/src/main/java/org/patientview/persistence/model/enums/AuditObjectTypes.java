package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
public enum AuditObjectTypes {

    User("User"),
    Group("Group");

    private String name;
    AuditObjectTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
