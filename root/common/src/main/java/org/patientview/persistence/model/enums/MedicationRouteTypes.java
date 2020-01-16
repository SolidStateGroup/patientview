package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's route types
 */
public enum MedicationRouteTypes {
    ORAL("Oral"),
    IV("IV"),
    IM("IM");

    private String name;
    MedicationRouteTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
