package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2015
 */
public enum SurveyResponseScoreTypes {
    SYMPTOM_SCORE("Symptom Score"),
    UNKNOWN("Unknown");

    private String name;
    SurveyResponseScoreTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
