package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public enum AuditActions {

    /*
    PatientView 1 audit actions (used in PatientView 2):
        PASSWORD_RESET_FORGOTTEN = "password reset forgotten";
        PASSWORD_RESET = "password reset";
        PASSWORD_CHANGE = "password change";
        PATIENT_DATA_FOLLOWUP = "patient data load"; // PATIENT_DATA_SUCCESS in pv2
        PATIENT_DATA_FAIL = "patient data fail";
        PATIENT_DATA_CORRUPT = "patient data corrupt"; // PATIENT_DATA_VALIDATE_FAIL in pv2 (not used in pv1)
        LOGGED_ON = "logon";
        PATIENT_ADD = "patient add";
        PATIENT_DELETE = "patient delete";  // used for both removing mapping AND user deletion in pv1
                                            // PATIENT_GROUP_ROLE_DELETE in pv2
        PATIENT_VIEW = "patient view";
        ADMIN_ADD = "admin add";
        EMAIL_VERIFY = "email verified";
        EMAIL_CHANGED = "email changed";
        PASSWORD_LOCKED = "password locked"; // ACCOUNT_LOCKED in pv2
        PASSWORD_UNLOCKED = "password unlocked"; // ACCOUNT_UNLOCKED in pv2
    
    PatientView 1 audit actions (not used in PatientView 2):
        ACTOR_SYSTEM = "system";
        PATIENT_DATA = "patient data";
        PATIENT_DATA_REMOVE = "patient data remove";
        PATIENT_HIDE = "patient hide";
        PATIENT_UNHIDE = "patient unhide";
        PATIENT_COUNT = "patient count";
        UKT_DATA_REPLACE = "ukt data";

    PatientView 1 live server audit actions logged in unitstats table, SELECT DISTINCT(action) FROM unitstats;
    pv2 group statistics lookups (SQL) shown after //

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

    // patient
    PATIENT_VIEW("Patient view"),
    PATIENT_ADD("Patient add"),
    PATIENT_EDIT("Patient edit"), // new in pv2
    PATIENT_DELETE("Patient delete"),
    PATIENT_GROUP_ROLE_ADD("Patient Group Role add"), // new in pv2
    PATIENT_GROUP_ROLE_DELETE("Patient Group Role delete"), // new in pv2
    PATIENT_LETTER_DELETE("Patient letter delete"), // new in pv2 (2.1.0)

    // admin
    ADMIN_ADD("Admin add"),
    ADMIN_EDIT("Admin edit"), // new in pv2
    ADMIN_DELETE("Admin delete"), // new in pv2
    ADMIN_GROUP_ROLE_ADD("Admin Group Role add"), // new in pv2
    ADMIN_GROUP_ROLE_DELETE("Admin Group Role delete"), // new in pv2

    // user
    PASSWORD_RESET_FORGOTTEN("Password reset forgotten"),
    PASSWORD_RESET("Password reset"),
    PASSWORD_CHANGE("Password change"),
    EMAIL_VERIFY("Email verified"),
    EMAIL_CHANGED("Email changed"),
    LOGGED_ON("Logon"),

    // user (new in pv2)
    LOGON_FAIL("Log on failed"),
    LOGGED_OFF("Log off"),
    ACCOUNT_LOCKED("Account locked"), // PASSWORD_LOCKED in pv1
    ACCOUNT_UNLOCKED("Account unlocked"), // PASSWORD_UNLOCKED in pv1

    // group (new in pv2)
    GROUP_ADD("Group add"),
    GROUP_EDIT("Group edit"),

    // importer
    PATIENT_DATA_SUCCESS("Patient data imported"), // PATIENT_DATA_FOLLOWUP in pv1
    PATIENT_DATA_VALIDATE_FAIL("Patient data failed validation"), // PATIENT_DATA_CORRUPT in pv1? (not used in pv1)
    PATIENT_DATA_FAIL("Patient data failed import"),
    SURVEY_RESPONSE_SUCCESS("Survey response imported"),
    SURVEY_RESPONSE_VALIDATE_FAIL("Survey response failed validation"),

    // ECS (GP Medication)
    GET_PATIENT_IDENTIFIERS_ECS("Get Patient Identifiers (ECS)"),
    
    // membership request conversation created
    MEMBERSHIP_REQUEST_SENT("Membership request sent");

    private String name;
    AuditActions(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
