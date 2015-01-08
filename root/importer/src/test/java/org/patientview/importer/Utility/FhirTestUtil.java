package org.patientview.importer.Utility;

import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;

/**
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class FhirTestUtil {


    public static Patient createTestPatient(String nhsNumber) {
        Patient patient = new Patient();
        HumanName humanName = patient.addName();
        humanName.addGivenSimple("Patient");
        humanName.addFamilySimple("Test");
        humanName.setUse(new Enumeration<HumanName.NameUse>(HumanName.NameUse.usual));

        org.hl7.fhir.instance.model.Identifier identifier = patient.addIdentifier();
        identifier.setLabelSimple("NHS_NUMBER");
        identifier.setValueSimple(nhsNumber);

        return patient;

    }

}
