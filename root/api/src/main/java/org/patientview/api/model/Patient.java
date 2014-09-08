package org.patientview.api.model;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.Code;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/09/2014
 */
public class Patient extends BaseModel{

    private List<FhirCondition> fhirConditions;
    private FhirPatient fhirPatient;
    private FhirPractitioner fhirPractitioner;
    private Group group;
    private List<Code> diagnosisCodes;

    public Patient() {
    }

    public Patient(org.hl7.fhir.instance.model.Patient patient, Practitioner practitioner,
                   org.patientview.persistence.model.Group group, List<Condition> conditions) {
        setFhirPatient(new FhirPatient(patient));
        setFhirPractitioner(new FhirPractitioner(practitioner));
        setGroup(new Group(group));

        setFhirConditions(new ArrayList<FhirCondition>());
        for (Condition condition : conditions) {
            getFhirConditions().add(new FhirCondition(condition));
        }

        setDiagnosisCodes(new ArrayList<Code>());
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
}
