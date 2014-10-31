package org.patientview.persistence.model;

import java.util.List;

/**
 * Transport object used for user migration
 *
 * Created by jamesr@solidstategroup.com
 * Created on 24/10/2014
 */
public class MigrationUser {

    private boolean patient;

    // User
    private User user;

    // Observations
    private List<FhirObservation> observations;

    // Conditions (diagnosis)
    private List<FhirCondition> conditions;

    // Encounters (treatment and transplant status)
    private List<FhirEncounter> encounters;

    // MedicationStatements (medication)
    private List<FhirMedicationStatement> medicationStatements;

    public MigrationUser () {
    }

    public MigrationUser(User user) {
        this.user = user;
    }

    public boolean isPatient() {
        return patient;
    }

    public void setPatient(boolean patient) {
        this.patient = patient;
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
}
