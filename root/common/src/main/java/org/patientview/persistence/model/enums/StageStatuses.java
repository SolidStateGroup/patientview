package org.patientview.persistence.model.enums;

/**
 * StageStatuses enumerator denotes statuses
 * for org.patientview.persistence.model.Stage steps
 */
public enum StageStatuses {
    PENDING("Pending"),
    STARTED("Started"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
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
