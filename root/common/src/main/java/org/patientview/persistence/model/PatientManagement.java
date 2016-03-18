package org.patientview.persistence.model;

import java.util.List;

/**
 * PatientManagement, used when creating new users, initially for IBD
 * Created by jamesr@solidstategroup.com
 * Created on 17/03/2016
 */
public class PatientManagement {

    // used for surgeries
    private List<FhirEncounter> fhirEncounters;

    // used for selects and text fields
    private List<org.patientview.persistence.model.FhirObservation> fhirObservations;

    // used for diagnosis
    private FhirCondition fhirCondition;

    // used staff members (IBD nurse, named consultant)
    private List<FhirPractitioner> fhirPractitioners;

    // used for patient information, postcode etc
    private FhirPatient fhirPatient;

    public PatientManagement() {

    }

    public List<FhirEncounter> getFhirEncounters() {
        return fhirEncounters;
    }

    public void setFhirEncounters(List<FhirEncounter> fhirEncounters) {
        this.fhirEncounters = fhirEncounters;
    }

    public List<org.patientview.persistence.model.FhirObservation> getFhirObservations() {
        return fhirObservations;
    }

    public void setFhirObservations(List<FhirObservation> fhirObservations) {
        this.fhirObservations = fhirObservations;
    }

    public FhirCondition getFhirCondition() {
        return fhirCondition;
    }

    public void setFhirCondition(FhirCondition fhirCondition) {
        this.fhirCondition = fhirCondition;
    }

    public List<FhirPractitioner> getFhirPractitioners() {
        return fhirPractitioners;
    }

    public void setFhirPractitioners(List<FhirPractitioner> fhirPractitioners) {
        this.fhirPractitioners = fhirPractitioners;
    }

    public FhirPatient getFhirPatient() {
        return fhirPatient;
    }

    public void setFhirPatient(FhirPatient fhirPatient) {
        this.fhirPatient = fhirPatient;
    }
}
