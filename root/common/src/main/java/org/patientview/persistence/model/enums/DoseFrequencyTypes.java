package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's dose frequency types.
 */
public enum DoseFrequencyTypes {
    ONE_DAY("Once a day", "1/d"),
    TWO_DAY("2 a day", "2/d"),
    THREE_DAY("3 a day", "3/d"),
    FOUR_DAY("4 a day", "4/d");

    private String name;
    private String value;

    DoseFrequencyTypes(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return this.name();
    }
}
