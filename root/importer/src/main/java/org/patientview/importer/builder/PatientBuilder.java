package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;

/**
 * This is going to mapping between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class PatientBuilder {

    public static Patient create(Patientview oldPatient) {

        Patient newPatient = new Patient();
        createHumanName(newPatient, oldPatient);
        return newPatient;
    }

    private static Patient createHumanName(Patient newPatient, Patientview oldPatient) {
        newPatient.addName().addFamilySimple(oldPatient.getPatient().getPersonaldetails().getSurname());
        newPatient.addName().addGivenSimple(oldPatient.getPatient().getPersonaldetails().getForename());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
        newPatient.addName().setUse(nameUse);
        return newPatient;
    }

}
