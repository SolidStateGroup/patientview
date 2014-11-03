package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * Reduced Group information FhirDocumentReference, for transport use
 */
public class FhirDocumentReference extends BaseModel {

    private Date date;
    private String type;
    private String content;
    private BaseGroup group;

    public FhirDocumentReference() {
    }

    public FhirDocumentReference(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference) {
        this.date = fhirDocumentReference.getDate();
        this.type = fhirDocumentReference.getType();
        this.content = fhirDocumentReference.getContent();
        if (fhirDocumentReference.getGroup() != null){
            this.group = new BaseGroup(fhirDocumentReference.getGroup());
        }
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
}
