package org.patientview.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FhirMedicationStatementRange, representing a List of FhirMedicationStatement and a date range, all medications
 * that currently exist for this User and Group within the date range will be removed from FHIR.
 * Created by jamesr@solidstategroup.com
 * Created on 08/03/2016
 */
public class FhirMedicationStatementRange extends BaseImport {
    private Date endDate;
    private List<FhirMedicationStatement> medications = new ArrayList<>();
    private Date startDate;

    public FhirMedicationStatementRange() { }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<FhirMedicationStatement> getMedications() {
        return medications;
    }

    public void setMedications(List<FhirMedicationStatement> medications) {
        this.medications = medications;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
