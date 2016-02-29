package org.patientview.api.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 15/01/2016
 */
public enum FileTypes {
    CSV("csv"),
    PDF("pdf");

    private String name;
    FileTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
