package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public enum SurveyTypes {
    CROHNS_SYMPTOM_SCORE("Crohns Symptom Score"),
    COLITIS_SYMPTOM_SCORE("Colitis Symptom Score"),
    IBD_CONTROL("IBD Control Questionnaire");

    private String name;
    SurveyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
