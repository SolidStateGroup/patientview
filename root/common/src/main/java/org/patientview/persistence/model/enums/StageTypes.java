package org.patientview.persistence.model.enums;

/**
 * StageTypes enumerator denotes different steps
 * for org.patientview.persistence.model.Stage
 */
public enum StageTypes {
    CONSULTATION("Consultation"),
    TESTING("Testing"),
    REVIEW("Review"),
    FURTHER_TESTING("Further Testing"),
    PLANNING("Planning"),
    OPERATION("Operation"),
    POST_OPERATION("Post Operation");

    private String name;

    StageTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
