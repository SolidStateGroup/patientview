package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public enum SurveyTypes {
    CROHNS_SYMPTOM_SCORE("Crohns Symptom Score");

    private String name;
    SurveyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
