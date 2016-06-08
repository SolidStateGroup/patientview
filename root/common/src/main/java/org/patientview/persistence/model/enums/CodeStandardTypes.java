package org.patientview.persistence.model.enums;

/**
 * Used in testing etc where specific name of code standard type is used (e.g. PATIENTVIEW, EDTA). Stored in Lookups
 * in database.
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
public enum CodeStandardTypes {
    EDTA("EDTA"),
    ICD("ICD"),
    PATIENTVIEW("PatientView"),
    READ("READ"),
    SNOMED("SNOMED");

    private String name;
    CodeStandardTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
