package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/12/2014
 */
public enum TransplantStatus {
    A("Active"),
    S("Suspended"),
    T("Transplanted"),
    R("Not on list"),
    N("Not on list"),
    O("Not on list");

    private String name;
    TransplantStatus(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
