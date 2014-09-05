package org.patientview.api.model;

import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.persistence.model.BaseModel;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/09/2014
 */
public class Patient extends BaseModel{

    private org.hl7.fhir.instance.model.Patient fhirPatient;
    private org.hl7.fhir.instance.model.Practitioner fhirPractitioner;
    private Group group;

    public Patient() {
    }

    public Patient(org.hl7.fhir.instance.model.Patient patient, org.hl7.fhir.instance.model.Practitioner practitioner,
                   org.patientview.persistence.model.Group group) {
        setFhirPatient(patient);
        setFhirPractitioner(practitioner);
        setGroup(new Group(group));
    }

    public org.hl7.fhir.instance.model.Patient getFhirPatient() {
        return fhirPatient;
    }

    public void setFhirPatient(org.hl7.fhir.instance.model.Patient fhirPatient) {
        this.fhirPatient = fhirPatient;
    }

    public Practitioner getFhirPractitioner() {
        return fhirPractitioner;
    }

    public void setFhirPractitioner(Practitioner fhirPractitioner) {
        this.fhirPractitioner = fhirPractitioner;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
