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
public class FhirDocumentReference extends BaseModel {

    private Date date;
    private String type;
    private String content;
    private Group group;

    // used by migration
    private String identifier;

    // only present for DocumentReference with associated Media and binary FileData
    private String filename;
    private String filetype;
    private Long fileDataId;
    private Long filesize;

    public FhirDocumentReference() {
    }

    public FhirDocumentReference(DocumentReference documentReference, Group group)
            throws FhirResourceException {

        if (documentReference.getCreated() != null) {
            DateTime created = documentReference.getCreated();
            DateAndTime date = created.getValue();
            setDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
        }

        if (documentReference.getType() == null) {
            throw new FhirResourceException("Cannot convert FHIR DocumentReference, missing Type");
        }

        if (documentReference.getDescription() != null) {
            setContent(documentReference.getDescriptionSimple());
        }

        setType(documentReference.getType().getTextSimple());
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public Long getFileDataId() {
        return fileDataId;
    }

    public void setFileDataId(Long fileDataId) {
        this.fileDataId = fileDataId;
    }
}
