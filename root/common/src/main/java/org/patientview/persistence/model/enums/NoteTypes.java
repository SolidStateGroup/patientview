package org.patientview.persistence.model.enums;

/**
 * NoteTypes enumerator denotes note type
 * for @link org.patientview.persistence.model.Note
 *
 */
public enum NoteTypes {
    DONORVIEW("Donor View");

    private String name;

    NoteTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
