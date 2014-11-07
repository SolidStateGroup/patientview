package org.patientview.api.model;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/09/2014
 */
public class Patient extends BaseModel {

    private List<FhirCondition> fhirConditions = new ArrayList<>();
    private FhirPatient fhirPatient;
    private FhirPractitioner fhirPractitioner;
    private Group group;
    private List<Code> diagnosisCodes = new ArrayList<>();
    private List<FhirEncounter> fhirEncounters = new ArrayList<>();
    private List<FhirObservation> fhirObservations = new ArrayList<>();

    public Patient() {
    }

    public Patient(org.hl7.fhir.instance.model.Patient patient, Practitioner practitioner,
                   org.patientview.persistence.model.Group group) {

        setFhirPatient(new FhirPatient(patient));
        if (practitioner != null) {
            setFhirPractitioner(new FhirPractitioner(practitioner));
        } else {
            setFhirPractitioner(new FhirPractitioner());
        }
        setGroup(new Group(group));
    }

    public FhirPatient getFhirPatient() {
        return fhirPatient;
    }

    public void setFhirPatient(FhirPatient fhirPatient) {
        this.fhirPatient = fhirPatient;
    }

    public FhirPractitioner getFhirPractitioner() {
        return fhirPractitioner;
    }

    public void setFhirPractitioner(FhirPractitioner fhirPractitioner) {
        this.fhirPractitioner = fhirPractitioner;
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
