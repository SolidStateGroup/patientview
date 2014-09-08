package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public enum IdentifierTypes {
    NHS_NUMBER("NHS Number"),
    CHI_NUMBER("CHI Number"),
    HOSPITAL_NUMBER("Hospital Number");

    private String name;
    IdentifierTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
