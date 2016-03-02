package org.patientview.builder;

import generated.Patientview;
import generated.PvDiagnosis;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
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
                    if (StringUtils.isNotEmpty(diagnosis.getValue())) {
                        try {
                            conditions.add(createCondition(diagnosis,
                                    data.getPatient().getClinicaldetails().getDiagnosisdate()));
                            success++;
                        } catch (FhirResourceException e) {
                            LOG.error("Invalid data in XML: " + e.getMessage());
                        }
                        count++;
                    }
                }
            }

            // edta diagnosis <diagnosisedta>, linked to codes
            if (StringUtils.isNotEmpty(data.getPatient().getClinicaldetails().getDiagnosisedta())) {
                try {
                    conditions.add(createCondition(data.getPatient().getClinicaldetails().getDiagnosisedta(),
                            data.getPatient().getClinicaldetails().getDiagnosisdate()));
                    success++;
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
                count++;
            }
        }

        return conditions;
    }

    private Condition createCondition(PvDiagnosis diagnosis, XMLGregorianCalendar date) throws FhirResourceException{
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

        // date required for IBD
        if (date != null) {
            DateAndTime dateAndTime = new DateAndTime(new Date(date.toGregorianCalendar().getTimeInMillis()));
            condition.setDateAssertedSimple(dateAndTime);
        }

        return condition;
    }

    private Condition createCondition(String edtaDiagnosis, XMLGregorianCalendar date) throws FhirResourceException{
        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(resourceReference);
        condition.setNotesSimple(CommonUtils.cleanSql(edtaDiagnosis));

        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(CommonUtils.cleanSql(edtaDiagnosis));
        condition.setCode(code);

        CodeableConcept category = new CodeableConcept();
        category.setTextSimple(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
        condition.setCategory(category);

        if (date != null) {
            DateAndTime dateAndTime = new DateAndTime(new Date(date.toGregorianCalendar().getTimeInMillis()));
            condition.setDateAssertedSimple(dateAndTime);
        }

        return condition;
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }
}
