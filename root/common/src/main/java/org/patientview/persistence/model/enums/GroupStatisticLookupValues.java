package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/12/2014
 */
public enum GroupStatisticLookupValues {

    /*
    PatientView 1 live server audit actions logged in unitstats table, SELECT DISTINCT(action) FROM unitstats;
    pv2 group statistics lookups (SQL) shown after //
    Used for migration of unit statistics from pv1 action=String to pv2 lookups

        admin add // ADMIN_GROUP_ROLE_ADD_COUNT
        email changed // EMAIL_CHANGED_COUNT
        email verified // EMAIL_VERIFY_COUNT
        logon // LOGGED_ON_COUNT
        password change // PASSWORD_CHANGE_COUNT
        password locked // ACCOUNT_LOCKED_COUNT
        password reset // PASSWORD_RESET_COUNT
        password reset forgotten // PASSWORD_RESET_FORGOTTEN_COUNT
        password unlocked // ACCOUNT_UNLOCKED_COUNT
        patient add // PATIENT_GROUP_ROLE_ADD_COUNT
        patient data fail // PATIENT_DATA_FAIL_COUNT
        patient data load // PATIENT_DATA_SUCCESS_COUNT
        patient data remove // not in pv2
        patient delete // PATIENT_GROUP_ROLE_DELETE_COUNT
        patient hide // not in pv2
        patient unhide // not in pv2
        patient view // PATIENT_VIEW_COUNT
        ukt data // not in pv2
        unique data load // UNIQUE_PATIENT_DATA_SUCCESS_COUNT
        unique logon // UNIQUE_LOGGED_ON_COUNT
     */

    ADMIN_GROUP_ROLE_ADD_COUNT("admin add"),
    EMAIL_CHANGED_COUNT("email changed"),
    EMAIL_VERIFY_COUNT("email verified"),
    LOGGED_ON_COUNT("logon"),
    PASSWORD_CHANGE_COUNT("password change"),
    ACCOUNT_LOCKED_COUNT("password locked"),
    PASSWORD_RESET_COUNT("password reset"),
    PASSWORD_RESET_FORGOTTEN_COUNT("password reset forgotten"),
    ACCOUNT_UNLOCKED_COUNT("password unlocked"),
    PATIENT_GROUP_ROLE_ADD_COUNT("patient add"),
    PATIENT_DATA_FAIL_COUNT("patient data fail"),
    PATIENT_DATA_SUCCESS_COUNT("patient data load"),
    PATIENT_GROUP_ROLE_DELETE_COUNT("patient delete"),
    PATIENT_VIEW_COUNT("patient view"),
    UNIQUE_PATIENT_DATA_SUCCESS_COUNT("unique data load"),
    UNIQUE_LOGGED_ON_COUNT("unique logon"),

    // from userlog table (done manually)
    PATIENT_COUNT("patient count");

    private String name;
    GroupStatisticLookupValues(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
