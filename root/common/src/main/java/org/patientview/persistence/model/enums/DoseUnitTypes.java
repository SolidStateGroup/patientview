package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's dose units types
 */
public enum DoseUnitTypes {
    MG("Mg"),
    G("g"),
    UI("Ui");

    private String name;
    DoseUnitTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
