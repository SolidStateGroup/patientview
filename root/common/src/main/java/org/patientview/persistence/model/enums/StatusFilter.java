package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/02/2015
 * for filtering users by status (e.g. locked, active, inactive)
 */
public enum StatusFilter {
    ACTIVE("active"),
    INACTIVE("inactive"),
    LOCKED("locked");

    private String name;
    StatusFilter(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
