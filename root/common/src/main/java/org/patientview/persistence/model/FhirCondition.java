package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirCondition extends BaseModel {

    private String code;
    // DiagnosisTypes.DIAGNOSIS, DiagnosisTypes.DIAGNOSIS_EDTA etc
    private String category;
    private String notes;
    private Date date;
    // used to denote main diagnosis, DiagnosisSeverityTypes.MAIN
    private String severity;

    // only used by migration
    private Group group;
    private String identifier;

    // used by my IBD and patient management to insert links
    private Set<Link> links;

    // pulled from Code if present (set separately to constructor)
    private String description;

    // used for displaying staff member who entered condition if present
    private String asserter;

    // used to set deletion status (confirmed = ok, refuted = deleted) for staff entered diagnosis
    private String status;

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

        if (condition.getStatusSimple() != null) {
            setStatus(condition.getStatusSimple().toCode());
        }

        if (condition.getSeverity() != null) {
            setSeverity(condition.getSeverity().getTextSimple());
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

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAsserter() {
        return asserter;
    }

    public void setAsserter(String asserter) {
        this.asserter = asserter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
