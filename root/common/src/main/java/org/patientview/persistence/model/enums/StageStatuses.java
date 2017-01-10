package org.patientview.persistence.model.enums;

/**
 * PathwayTypes enumerator denotes pathway type
 * for org.patientview.persistence.model.Pathway
 */
public enum StageStatuses {
    PENDING("Pending"),
    STARTED("Started"),
    ON_HOLD("On Hold"),
    COMPLETE("Complete"),
    STOPPED("Stopped");

    private String name;

    StageStatuses(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
