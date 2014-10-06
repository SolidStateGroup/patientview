package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
public enum DiagnosticResultTypes {
    DIAGNOSTIC_RESULT("Diagnostic Result");

    private String name;
    DiagnosticResultTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
