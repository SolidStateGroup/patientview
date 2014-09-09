package org.patientview.api.model;

import org.hl7.fhir.instance.model.Condition;
import org.patientview.persistence.model.BaseModel;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirCondition extends BaseModel{

    private String code;
    private String category;
    private String notes;

    public FhirCondition() {
    }

    public FhirCondition(Condition condition) {
        if (condition.getCode() != null) {
            setCode(condition.getCode().getTextSimple());
        }
        if (condition.getCategory() != null) {
            setCategory(condition.getCategory().getTextSimple());
        }
        setNotes(condition.getNotesSimple());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
