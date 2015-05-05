package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * FhirDocumentReference, representing a letter and the associated Group which provided the data.
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 */
public class FhirDocumentReference extends BaseModel {

    private Date date;
    private String type;
    private String content;
    private BaseGroup group;

    // only present for DocumentReference with associated Media and binary FileData
    private String filename;
    private String filetype;
    private Long fileDataId;

    public FhirDocumentReference() {
    }

    public FhirDocumentReference(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference) {
        this.date = fhirDocumentReference.getDate();
        this.type = fhirDocumentReference.getType();
        this.content = fhirDocumentReference.getContent();
        if (fhirDocumentReference.getGroup() != null) {
            this.group = new BaseGroup(fhirDocumentReference.getGroup());
        }
        this.filename = fhirDocumentReference.getFilename();
        this.filetype = fhirDocumentReference.getFiletype();
        this.fileDataId = fhirDocumentReference.getFileDataId();
    }

    public Date getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public String getFilename() {
        return filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public Long getFileDataId() {
        return fileDataId;
    }
}
