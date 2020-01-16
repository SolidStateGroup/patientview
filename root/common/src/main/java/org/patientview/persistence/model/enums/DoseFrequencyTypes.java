package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's dose frequency types.
 */
public enum DoseFrequencyTypes {
    ONE_DAY("Once a day"),
    TWO_DAY("2 a day"),
    THREE_DAY("3 a day"),
    FOUR_DAY("4 a day");

    private String name;
    DoseFrequencyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
