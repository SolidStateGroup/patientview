package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirCondition extends BaseModel {

    private String code;
    private String category;
    private String notes;
    private Date date;

    // only used by migration
    private Group group;
    private String identifier;

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

        if (condition.getDateAssertedSimple() != null) {
            DateAndTime date = condition.getDateAssertedSimple();
            setDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
        }
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
