package org.patientview.persistence.model.enums;

/**
 * Source of Code, PATIENTVIEW by default, NHS_CHOICES if from NHS Choices API.
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
public enum CodeSourceTypes {
    PATIENTVIEW("PatientView"),
    NHS_CHOICES("NHS Choices");

    private String name;
    CodeSourceTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
