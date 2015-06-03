package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/06/2015
 */
public class FhirAllergy extends BaseModel {

    private String reaction;
    private Date recordedDate;
    private String status;
    private String substance;
    private String type;

    public FhirAllergy() {
    }

    // if converting from actual Observation
    public FhirAllergy(AllergyIntolerance allergyIntolerance, Substance substance, AdverseReaction adverseReaction)
            throws FhirResourceException {

        if (allergyIntolerance == null) {
            throw new FhirResourceException("Cannot convert FHIR Allergy, missing AllergyIntolerance");
        }

        if (allergyIntolerance.getStatus() != null) {
            setStatus(allergyIntolerance.getStatusSimple().toString());
        }

        if (allergyIntolerance.getRecordedDate() != null) {
            DateAndTime date = allergyIntolerance.getRecordedDate().getValue();
            setRecordedDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
        }

        if (substance != null) {
            if (substance.getDescription() != null) {
                setSubstance(substance.getDescriptionSimple());
            }
            if (substance.getType() != null) {
                setType(substance.getType().getTextSimple());
            }
        }

        if (adverseReaction != null && !CollectionUtils.isEmpty(adverseReaction.getSymptom())) {
            AdverseReaction.AdverseReactionSymptomComponent symptom = adverseReaction.getSymptom().get(0);
            if (symptom != null && symptom.getCode() != null) {
                setReaction(symptom.getCode().getTextSimple());
            }
        }
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubstance() {
        return substance;
    }

    public void setSubstance(String substance) {
        this.substance = substance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
