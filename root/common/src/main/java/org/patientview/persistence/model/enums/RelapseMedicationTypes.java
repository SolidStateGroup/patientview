package org.patientview.persistence.model.enums;

/**
 * Enum for Relapse Medication types.
 */
public enum RelapseMedicationTypes {
    ORAL_PREDNISOLONE("Oral Prednisolone,"),
    METHYL_ORAL_PREDNISOLONE("Methyl Prednisolone"),
    OTHER("Other");

    private String name;
    RelapseMedicationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
