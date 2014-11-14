package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public enum AuditActions {

    CREATE("Create"),
    EDIT("Edit"),
    VIEW("View"),
    CHANGE_PASSWORD("Change Password"),
    DELETE("Delete User"),
    RESET_PASSWORD("Reset Password"),
    VERIFY_EMAIL("Verify Email"),
    LOGON_SUCCESS("Logon Success"),
    MAP("Map"),
    LOGON_FAIL("Logon failed"),
    LOGOFF("Log off"),
    SWITCH_USER("Switch User"),

    // importer
    PATIENT_DATA_SUCCESS("Patient data imported"),
    PATIENT_DATA_VALIDATE_FAIL("Patient data failed validation"),
    PATIENT_DATA_FAIL("Patient data failed import");

    private String name;
    AuditActions(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
