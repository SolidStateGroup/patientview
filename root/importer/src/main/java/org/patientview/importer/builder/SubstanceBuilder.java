package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Substance;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class SubstanceBuilder {

    private Patientview.Patient.Allergy allergyData;

    public SubstanceBuilder(Patientview.Patient.Allergy allergyData) {
        this.allergyData = allergyData;
    }

    public Substance build() {
        Substance substance = new Substance();

        if (allergyData.getAllergytypecode() != null) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(allergyData.getAllergytypecode());
            substance.setType(type);
        }

        if (allergyData.getAllergysubstance() != null) {
            substance.setDescriptionSimple(allergyData.getAllergysubstance());
        }

        return substance;
    }
}
