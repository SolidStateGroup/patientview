package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public enum DiagnosisSeverityTypes {
    MAIN("Main diagnosis");

    private String name;
    DiagnosisSeverityTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
