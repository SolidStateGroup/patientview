package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/09/2014
 */
public class Patient extends BaseModel{

    private org.hl7.fhir.instance.model.Patient fhirPatient;
    private Group group;

    public Patient() {
    }

    public Patient(org.hl7.fhir.instance.model.Patient patient, org.patientview.persistence.model.Group group) {
        setFhirPatient(patient);
        setGroup(new Group(group));
    }

    public org.hl7.fhir.instance.model.Patient getFhirPatient() {
        return fhirPatient;
    }

    public void setFhirPatient(org.hl7.fhir.instance.model.Patient fhirPatient) {
        this.fhirPatient = fhirPatient;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
