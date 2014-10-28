package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
public enum DiagnosticReportTypes {
    IMAGING("Imaging"),
    ENDOSCOPY("Endoscopy");

    private String name;
    DiagnosticReportTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
