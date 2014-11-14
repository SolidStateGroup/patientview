package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
public enum RestrictedUsernames {
    SYSTEM("system"),
    GLOBALADMIN("globladmin"),
    MIGRATION("migration"),
    IMPORTER("importer");

    private String name;
    RestrictedUsernames(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
