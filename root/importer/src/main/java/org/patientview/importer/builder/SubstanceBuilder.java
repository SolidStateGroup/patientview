package org.patientview.importer.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.config.utils.CommonUtils;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class SubstanceBuilder {

    private Patientview.Patient.Allergydetails.Allergy allergyData;

    public SubstanceBuilder(Patientview.Patient.Allergydetails.Allergy allergyData) {
        this.allergyData = allergyData;
    }

    public Substance build() {
        Substance substance = new Substance();

        if (StringUtils.isNotEmpty(allergyData.getAllergytypecode())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(CommonUtils.cleanSql(allergyData.getAllergytypecode()));
            substance.setType(type);
        }

        if (StringUtils.isNotEmpty(allergyData.getAllergysubstance())) {
            substance.setDescriptionSimple(CommonUtils.cleanSql(allergyData.getAllergysubstance()));
        }

        return substance;
    }
}
