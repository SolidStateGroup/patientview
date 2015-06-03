package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Patient, representing several patient related objects, retrieved from FHIR and associated with certain Groups.
 * Created by jamesr@solidstategroup.com
 * Created on 03/09/2014
 */
public class Patient extends BaseModel {

    private List<FhirCondition> fhirConditions = new ArrayList<>();
    private FhirPatient fhirPatient;
    private List<FhirPractitioner> fhirPractitioners = new ArrayList<>();
    private Group group;
    private List<Code> diagnosisCodes = new ArrayList<>();
    private List<FhirEncounter> fhirEncounters = new ArrayList<>();
    private List<FhirObservation> fhirObservations = new ArrayList<>();

    public Patient() {
    }

    public Patient(org.hl7.fhir.instance.model.Patient patient, org.patientview.persistence.model.Group group) {
        setFhirPatient(new FhirPatient(patient));
        setGroup(new Group(group));
    }

    public FhirPatient getFhirPatient() {
        return fhirPatient;
    }

    public void setFhirPatient(FhirPatient fhirPatient) {
        this.fhirPatient = fhirPatient;
    }

    public List<FhirPractitioner> getFhirPractitioners() {
        return fhirPractitioners;
    }

    public void setFhirPractitioners(List<FhirPractitioner> fhirPractitioners) {
        this.fhirPractitioners = fhirPractitioners;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<FhirCondition> getFhirConditions() {
        return fhirConditions;
    }

    public void setFhirConditions(List<FhirCondition> fhirConditions) {
        this.fhirConditions = fhirConditions;
    }

    public List<Code> getDiagnosisCodes() {
        return diagnosisCodes;
    }

    public void setDiagnosisCodes(List<Code> diagnosisCodes) {
        this.diagnosisCodes = diagnosisCodes;
    }

    public List<FhirEncounter> getFhirEncounters() {
        return fhirEncounters;
    }

    public void setFhirEncounters(List<FhirEncounter> fhirEncounters) {
        this.fhirEncounters = fhirEncounters;
    }

    public List<FhirObservation> getFhirObservations() {
        return fhirObservations;
    }

    public void setFhirObservations(List<FhirObservation> fhirObservations) {
        this.fhirObservations = fhirObservations;
    }
}
