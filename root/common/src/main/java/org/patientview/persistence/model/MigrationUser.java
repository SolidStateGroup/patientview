package org.patientview.persistence.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Transport object used for user migration
 *
 * Created by jamesr@solidstategroup.com
 * Created on 24/10/2014
 */
public class MigrationUser {

    private Long patientview1Id;
    private boolean patient;
    private Long observationStartDate;
    private Long observationEndDate;

    // User
    private User user;

    // Observations
    private List<FhirObservation> observations = new ArrayList<>();

    // Conditions (diagnosis)
    private List<FhirCondition> conditions = new ArrayList<>();

    // Encounters (treatment and transplant status)
    private List<FhirEncounter> encounters = new ArrayList<>();

    // MedicationStatements (medication)
    private List<FhirMedicationStatement> medicationStatements = new ArrayList<>();

    // DiagnosticReports
    private List<FhirDiagnosticReport> diagnosticReports = new ArrayList<>();

    // DocumentReferences (letters)
    private List<FhirDocumentReference> documentReferences = new ArrayList<>();

    // Patient (pv1 patient table data)
    private List<FhirPatient> patients = new ArrayList<>();

    public MigrationUser () {
    }

    public MigrationUser(User user) {
        this.user = user;
    }

    public Long getPatientview1Id() {
        return patientview1Id;
    }

    public void setPatientview1Id(Long patientview1Id) {
        this.patientview1Id = patientview1Id;
    }

    public boolean isPatient() {
        return patient;
    }

    public void setPatient(boolean patient) {
        this.patient = patient;
    }

    public Long getObservationStartDate() {
        return observationStartDate;
    }

    public void setObservationStartDate(Long observationStartDate) {
        this.observationStartDate = observationStartDate;
    }

    public Long getObservationEndDate() {
        return observationEndDate;
    }

    public void setObservationEndDate(Long observationEndDate) {
        this.observationEndDate = observationEndDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<FhirObservation> getObservations() {
        return observations;
    }

    public void setObservations(List<FhirObservation> observations) {
        this.observations = observations;
    }

    public List<FhirCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<FhirCondition> conditions) {
        this.conditions = conditions;
    }

    public List<FhirEncounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<FhirEncounter> encounters) {
        this.encounters = encounters;
    }

    public List<FhirMedicationStatement> getMedicationStatements() {
        return medicationStatements;
    }

    public void setMedicationStatements(List<FhirMedicationStatement> medicationStatements) {
        this.medicationStatements = medicationStatements;
    }

    public List<FhirDiagnosticReport> getDiagnosticReports() {
        return diagnosticReports;
    }

    public void setDiagnosticReports(List<FhirDiagnosticReport> diagnosticReports) {
        this.diagnosticReports = diagnosticReports;
    }

    public List<FhirDocumentReference> getDocumentReferences() {
        return documentReferences;
    }

    public void setDocumentReferences(List<FhirDocumentReference> documentReferences) {
        this.documentReferences = documentReferences;
    }

    public List<FhirPatient> getPatients() {
        return patients;
    }

    public void setPatients(List<FhirPatient> patients) {
        this.patients = patients;
    }
}
