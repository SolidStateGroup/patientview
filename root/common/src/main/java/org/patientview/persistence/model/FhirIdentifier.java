package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.Identifier;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirIdentifier extends BaseModel {

    private String label;
    private String value;

    public FhirIdentifier() {
    }
    
    public FhirIdentifier(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public FhirIdentifier(Identifier identifier) {
        this.label = identifier.getLabelSimple();
        this.value = identifier.getValueSimple();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
