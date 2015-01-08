package org.patientview.persistence.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MigrationStatus {

    USER_STARTED("Started user migration"),
    USER_FAILED("Failed during user migration"),
    USER_MIGRATED("User data migrated"),
    PATIENT_STARTED("started patient data migration"),
    PATIENT_MIGRATED("Patient data migrated"),
    PATIENT_CLEANUP_FAILED("Failed during cleanup of patient data after failed migration"),
    PATIENT_FAILED("Failed during patient data migration"),
    OBSERVATIONS_STARTED("Started observation migration"),
    OBSERVATIONS_FAILED("Failed during observation migration"),
    OBSERVATIONS_MIGRATED("Observation data migrated"),
    COMPLETED("Completed");

    private String name;
    MigrationStatus(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }


}
