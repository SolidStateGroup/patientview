package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
public enum AlertTypes {
    RESULT("Result"),
    LETTER("Letter");

    private String name;
    AlertTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
