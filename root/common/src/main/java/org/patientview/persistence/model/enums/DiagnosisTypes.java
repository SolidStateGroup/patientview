package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public enum DiagnosisTypes {
    DIAGNOSIS("diagnosis"),
    DIAGNOSIS_EDTA("diagnosisedta"),
    DIAGNOSIS_PATIENT_ENTERED("diagnosispatiententered"),
    DIAGNOSIS_STAFF_ENTERED("diagnosisstaffentered");

    private String name;
    DiagnosisTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
