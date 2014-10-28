package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * Reduced Group information FhirLetter, for transport use
 */
public class FhirLetter extends BaseModel {

    private Date date;
    private String type;
    private String content;
    private BaseGroup group;

    public FhirLetter() {
    }

    public FhirLetter(org.patientview.persistence.model.FhirLetter fhirLetter) {
        this.date = fhirLetter.getDate();
        this.type = fhirLetter.getType();
        this.content = fhirLetter.getContent();
        if (fhirLetter.getGroup() != null){
            this.group = new BaseGroup(fhirLetter.getGroup());
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
