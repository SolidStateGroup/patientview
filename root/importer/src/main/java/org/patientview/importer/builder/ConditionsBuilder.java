package org.patientview.importer.builder;

import generated.Patientview;
import generated.PvDiagnosis;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
public class ConditionsBuilder {

    private final Logger LOG = LoggerFactory.getLogger(ConditionsBuilder.class);

    private ResourceReference resourceReference;
    private Patientview data;
    private List<Condition> conditions;
    private int success = 0;
    private int count = 0;

    public ConditionsBuilder(Patientview results, ResourceReference resourceReference) {
        this.data = results;
        this.resourceReference = resourceReference;
        conditions = new ArrayList<>();
    }

    // Normally and invalid data would fail the whole XML
    public List<Condition> build() {

        if (data.getPatient().getClinicaldetails() != null) {
            // generic other <diagnosis>
            if (data.getPatient().getClinicaldetails().getDiagnosis() != null) {
                for (PvDiagnosis diagnosis : data.getPatient().getClinicaldetails().getDiagnosis()) {
                    try {
                        conditions.add(createCondition(diagnosis));
                        success++;
                    } catch (FhirResourceException e) {
                        LOG.error("Invalid data in XML: " + e.getMessage());
                    }
                    count++;
                }
            }

            // edta diagnosis <diagnosisedta>, linked to codes
            if (data.getPatient().getClinicaldetails().getDiagnosisedta() != null) {
                try {
                    conditions.add(createCondition(data.getPatient().getClinicaldetails().getDiagnosisedta()));
                    success++;
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
                count++;
            }
        }

        return conditions;
    }

    private Condition createCondition(PvDiagnosis diagnosis) throws FhirResourceException{
        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(resourceReference);
        condition.setNotesSimple(diagnosis.getValue());

        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(diagnosis.getValue());
        condition.setCode(code);

        CodeableConcept category = new CodeableConcept();
        category.setTextSimple(DiagnosisTypes.DIAGNOSIS.toString());
        condition.setCategory(category);

        return condition;
    }

    private Condition createCondition(String edtaDiagnosis) throws FhirResourceException{
        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(resourceReference);
        condition.setNotesSimple(edtaDiagnosis);

        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(edtaDiagnosis);
        condition.setCode(code);

        CodeableConcept category = new CodeableConcept();
        category.setTextSimple(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
        condition.setCategory(category);

        return condition;
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }
}
