package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public enum LetterTypes {
    CLINIC_LETTER("Clinic Letter"),
    DISCHARGE_SUMMARY("Discharge Summary");

    private String name;
    LetterTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
