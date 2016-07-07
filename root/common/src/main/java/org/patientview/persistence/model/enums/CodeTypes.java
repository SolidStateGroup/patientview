package org.patientview.persistence.model.enums;

/**
 * Type of Code, currently only diagnosis and treatment. Stored in Lookups in database.
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
