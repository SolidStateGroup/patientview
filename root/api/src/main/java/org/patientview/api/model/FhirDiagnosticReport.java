package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * Reduced Group/Observation information FhirDiagnosticReport, for transport use
 */
public class FhirDiagnosticReport {

    private Long id;
    private Date date;
    private String name;
    private FhirObservation result;
    private String type;
    private BaseGroup group;

    public FhirDiagnosticReport() {
    }

    public FhirDiagnosticReport(org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport) {
        this.date = fhirDiagnosticReport.getDate();
        this.name = fhirDiagnosticReport.getName();
        if (fhirDiagnosticReport.getResult() != null) {
            this.result = new FhirObservation(fhirDiagnosticReport.getResult());
        }
        this.type = fhirDiagnosticReport.getType();
        if (fhirDiagnosticReport.getGroup() != null) {
            this.group = new BaseGroup(fhirDiagnosticReport.getGroup());
        }
    }

    public Long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public FhirObservation getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    public BaseGroup getGroup() {
        return group;
    }
}
