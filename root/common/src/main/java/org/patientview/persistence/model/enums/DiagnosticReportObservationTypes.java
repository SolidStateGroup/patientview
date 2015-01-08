package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 *
 * Observation types used to reference non test/result Observation records (for diagnostic results)
 */
public enum DiagnosticReportObservationTypes {
    DIAGNOSTIC_RESULT("Diagnostic Result");

    private String name;
    DiagnosticReportObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
