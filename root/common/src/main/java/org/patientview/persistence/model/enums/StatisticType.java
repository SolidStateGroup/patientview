package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public enum StatisticType {
    ACCOUNT_LOCKED_COUNT("Account locks"),
    ACCOUNT_UNLOCKED_COUNT("Account unlocks"),
    ADMIN_GROUP_ROLE_ADD_COUNT("Admin users added"),
    EMAIL_CHANGED_COUNT("Emails changed"),
    EMAIL_VERIFY_COUNT("Emails verified"),
    INACTIVE_USER_COUNT("Inactive users"),
    LOCKED_USER_COUNT("Locked users"),
    LOGGED_ON_COUNT("Logons"),
    PASSWORD_CHANGE_COUNT("Password changed count"),
    PASSWORD_RESET_COUNT("Password reset count"),
    PASSWORD_RESET_FORGOTTEN_COUNT("Forgotten password count"),
    PATIENT_COUNT("Patients"),
    PATIENT_DATA_FAIL_COUNT("Failed imports"),
    PATIENT_DATA_SUCCESS_COUNT("Successful imports"),
    PATIENT_GROUP_ROLE_ADD_COUNT("Patients added"),
    PATIENT_GROUP_ROLE_DELETE_COUNT("Patients removed"),
    PATIENT_VIEW_COUNT("Patients viewed"),
    UNIQUE_LOGGED_ON_COUNT("Unique logons"),
    UNIQUE_PATIENT_DATA_SUCCESS_COUNT("Unique successful imports");

    private String name;
    StatisticType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
