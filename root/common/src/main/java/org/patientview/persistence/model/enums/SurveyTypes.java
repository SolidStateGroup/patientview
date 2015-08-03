package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public enum SurveyTypes {
    CROHNS_SYMPTOM_SCORE("Crohns Symptom Score"),
    COLITIS_SYMPTOM_SCORE("Colitis Symptom Score"),
    IBD_CONTROL("IBD Control Questionnaire"),
    HEART_SYMPTOM_SCORE("Heart Symptom Score");

    private String name;
    SurveyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
