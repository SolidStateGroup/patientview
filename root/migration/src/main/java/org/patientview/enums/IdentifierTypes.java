package org.patientview.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/10/2014
 */
public enum IdentifierTypes {
    NHS_NUMBER("NHS Number"),
    CHI_NUMBER("CHI Number"),
    HSC_NUMBER("H&SC Number"),
    HOSPITAL_NUMBER("Hospital Number");

    private String name;
    IdentifierTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
