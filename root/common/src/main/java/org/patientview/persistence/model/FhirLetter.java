package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.patientview.config.exception.FhirResourceException;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public class FhirLetter extends BaseModel {

    private Date date;
    private String type;
    private String content;
    private Group group;

    public FhirLetter() {
    }

    public FhirLetter(DocumentReference documentReference, org.patientview.persistence.model.Group group)
            throws FhirResourceException {

        if (documentReference.getCreated() == null) {
            throw new FhirResourceException("Cannot convert FHIR DocumentReference, missing Created");
        }

        if (documentReference.getType() == null) {
            throw new FhirResourceException("Cannot convert FHIR DocumentReference, missing Type");
        }

        if (documentReference.getDescription() == null) {
            throw new FhirResourceException("Cannot convert FHIR DocumentReference, missing Description (content)");
        }

        DateTime created = documentReference.getCreated();
        DateAndTime date = created.getValue();
        setDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));

        setType(documentReference.getType().getTextSimple());
        setContent(documentReference.getDescriptionSimple());
        setGroup(group);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
