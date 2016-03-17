package org.patientview.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FhirDiagnosticReportRange, representing a List of FhirDiagnosticReport and a date range, all diagnostics
 * that currently exist for this User and Group within the date range will be removed from FHIR.
 * Created by jamesr@solidstategroup.com
 * Created on 09/03/2016
 */
public class FhirDiagnosticReportRange extends BaseImport {
    private Date endDate;
    private List<FhirDiagnosticReport> diagnostics = new ArrayList<>();
    private Date startDate;

    public FhirDiagnosticReportRange() { }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<FhirDiagnosticReport> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<FhirDiagnosticReport> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
