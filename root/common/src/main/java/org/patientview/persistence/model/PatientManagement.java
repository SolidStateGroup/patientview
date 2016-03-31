package org.patientview.persistence.model;

import java.util.List;

/**
 * PatientManagement, used when creating new users and updating existing user's patient management info,
 * initially for IBD.
 * Created by jamesr@solidstategroup.com
 * Created on 17/03/2016
 */
public class PatientManagement extends BaseImport {

    // used for surgeries
    private List<FhirEncounter> encounters;

    // used for selects and text fields
    private List<org.patientview.persistence.model.FhirObservation> observations;

    // used for diagnosis
    private FhirCondition condition;

    // used staff members (IBD nurse, named consultant)
    private List<FhirPractitioner> practitioners;

    // used for patient information, postcode etc
    private FhirPatient patient;

    public PatientManagement() {

    }

    public List<FhirEncounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<FhirEncounter> encounters) {
        this.encounters = encounters;
    }

    public List<FhirObservation> getObservations() {
        return observations;
    }

    public void setObservations(List<FhirObservation> observations) {
        this.observations = observations;
    }

    public FhirCondition getCondition() {
        return condition;
    }

    public void setCondition(FhirCondition condition) {
        this.condition = condition;
    }

    public List<FhirPractitioner> getPractitioners() {
        return practitioners;
    }

    public void setPractitioners(List<FhirPractitioner> practitioners) {
        this.practitioners = practitioners;
    }

    public FhirPatient getPatient() {
        return patient;
    }

    public void setPatient(FhirPatient patient) {
        this.patient = patient;
    }
}
