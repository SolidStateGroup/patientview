package org.patientview.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirCondition;

/**
 * Build Condition object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update. For Date, clear if null.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class ConditionBuilder {

    private Condition condition;
    private FhirCondition fhirCondition;
    private ResourceReference patientReference;

    public ConditionBuilder(Condition condition, FhirCondition fhirCondition, ResourceReference patientReference) {
        this.condition = condition;
        this.fhirCondition = fhirCondition;
        this.patientReference = patientReference;
    }

    public Condition build() {
        if (condition == null) {
            condition = new Condition();
        }

        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(patientReference);

        // set notes
        if (StringUtils.isNotEmpty(fhirCondition.getNotes())) {
            condition.setNotesSimple(fhirCondition.getNotes());
        } else {
            // if code isn't empty and not the same as notes
            if (StringUtils.isNotEmpty(fhirCondition.getCode())
                    && !fhirCondition.getCode().equals(fhirCondition.getNotes())) {
                condition.setNotesSimple(fhirCondition.getCode());
            }
        }

        // code
        if (fhirCondition.getCode() != null) {
            if (StringUtils.isNotEmpty(fhirCondition.getCode())) {
                // set code
                CodeableConcept code = new CodeableConcept();
                code.setTextSimple(CommonUtils.cleanSql(fhirCondition.getCode()));
                condition.setCode(code);
            } else {
                // clear existing code
                condition.setCode(null);
            }
        }

        // notes
        if (fhirCondition.getNotes() != null) {
            if (StringUtils.isNotEmpty(fhirCondition.getNotes())) {
                // set notes
                condition.setNotesSimple(CommonUtils.cleanSql(fhirCondition.getNotes()));
            } else {
                // clear existing notes
                condition.setNotes(null);
            }
        }

        // category (DiagnosisTypes.DIAGNOSIS etc)
        if (fhirCondition.getCategory() != null) {
            if (StringUtils.isNotEmpty(fhirCondition.getCategory())) {
                // set category
                CodeableConcept category = new CodeableConcept();
                category.setTextSimple(CommonUtils.cleanSql(fhirCondition.getCategory()));
                condition.setCategory(category);
            } else {
                // clear existing category
                condition.setCategory(null);
            }
        }

        // severity (DiagnosisSeverityTypes.MAIN etc)
        if (fhirCondition.getSeverity() != null) {
            if (StringUtils.isNotEmpty(fhirCondition.getSeverity())) {
                // set severity
                CodeableConcept severity = new CodeableConcept();
                severity.setTextSimple(CommonUtils.cleanSql(fhirCondition.getSeverity()));
                condition.setSeverity(severity);
            } else {
                // clear existing severity
                condition.setSeverity(null);
            }
        }

        // date
        if (fhirCondition.getDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirCondition.getDate());
            condition.setDateAssertedSimple(dateAndTime);
        }

        return condition;
    }
}
