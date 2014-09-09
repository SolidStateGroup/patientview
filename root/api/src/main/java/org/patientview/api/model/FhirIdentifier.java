package org.patientview.api.model;

import org.hl7.fhir.instance.model.Identifier;
import org.patientview.persistence.model.BaseModel;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirIdentifier extends BaseModel{

    private String label;
    private String value;

    public FhirIdentifier() {
    }

    public FhirIdentifier(Identifier identifier) {
        setLabel(identifier.getLabelSimple());
        setValue(identifier.getValueSimple());
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
