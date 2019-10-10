package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum FeatureType {

    MESSAGING("Messaging"), 
    FEEDBACK("Feedback"),
    GP_MEDICATION("GP Medication"), 
    UNIT_TECHNICAL_CONTACT("Unit Technical Contact"), 
    PATIENT_SUPPORT_CONTACT("Patient Support Contact"), 
    DEFAULT_MESSAGING_CONTACT("Default Messaging Contact"),
    CENTRAL_SUPPORT_CONTACT("Central Support Contact"),
    IBD_SCORING_ALERTS("IBD Scoring Alerts"),
    IBD_PATIENT_MANAGEMENT("IBD Patient Management"),
    RENAL_SURVEY_FEEDBACK_RECIPIENT("Renal Survey Feedback Recipient"),
    RENAL_HEALTH_SURVEYS("Renal Survey Feedback Recipient"),
    ENTER_OWN_DIAGNOSES("Enter Own Diagnoses"),
    OPT_EPRO("OPT_EPRO"),
    INS_DIARY("INS Diary Recording");

    private String name;
    FeatureType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
