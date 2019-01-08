package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public enum SurveyTypes {
    CROHNS_SYMPTOM_SCORE("Crohns Symptom Score"),
    COLITIS_SYMPTOM_SCORE("Colitis Symptom Score"),
    IBD_CONTROL("IBD Control Questionnaire"),
    HEART_SYMPTOM_SCORE("Heart Symptom Score"),
    IBD_FATIGUE("IBD Fatigue Questionnaire"),
    IBD_SELF_MANAGEMENT("IBD Self-Management Programme"),
    IBD_PATIENT_MANAGEMENT("IBD Patient Management Programme"),
    POS_S("POS_S");

    private String name;
    SurveyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
