package org.patientview.importer.builder;

import generated.Patientview.Patient.Drugdetails.Drug;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Medication;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class MedicationBuilder {

    private Drug data;

    public MedicationBuilder(Drug data) {
        this.data = data;
    }

    public Medication build() {
        Medication medication = new Medication();

        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(data.getDrugname());
        medication.setCode(code);

        return medication;
    }
}
