package org.patientview.persistence.model.enums;

/**
 * Enum for Urine Protein dipstick types for InsDiaryRecord recording
 */
public enum ProteinDipstickTypes {
    NEGATIVE("Negative"),
    TRACE("Trace"),
    ONE("One+"),
    TWO("Two+"),
    THREE("Three+"),
    FOUR("Four+");

    private String name;
    ProteinDipstickTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
