package org.patientview.persistence.model.enums;

/**
 * Enum for Oedema types for InsDiaryRecord recording.
 */
public enum OedemaTypes {
    NONE("none"),
    ANKLES("ankles"),
    LEGS("legs"),
    HANDS("hands"),
    ABDOMEN("abdomen"),
    NECK("neck"),
    EYES("eyes");

    private String name;
    OedemaTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
