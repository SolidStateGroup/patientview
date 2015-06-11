package org.patientview.persistence.model.enums;

/**
 * For SymptomScore severity (calculated)
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2015
 */
public enum ScoreSeverity {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    UNKNOWN("Unknown");

    private String name;
    ScoreSeverity(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
