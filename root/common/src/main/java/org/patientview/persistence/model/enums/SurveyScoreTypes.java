package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2015
 */
public enum SurveyScoreTypes {
    SYMPTOM_SCORE("Symptom Score");

    private String name;
    SurveyScoreTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
