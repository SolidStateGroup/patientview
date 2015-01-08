package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public enum CodeTypes {
    DIAGNOSIS("Diagnosis"),
    TREATMENT("Treatment");

    private String name;
    CodeTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
