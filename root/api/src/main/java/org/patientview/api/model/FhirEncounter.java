package org.patientview.api.model;

import org.hl7.fhir.instance.model.Encounter;
import org.patientview.persistence.model.BaseModel;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirEncounter extends BaseModel{

    private String identifier;
    private String type;

    public FhirEncounter() {
    }

    public FhirEncounter(Encounter encounter) {
        if (!encounter.getIdentifier().isEmpty()) {
            setIdentifier(encounter.getIdentifier().get(0).getValue().getValue());
        }
        if (!encounter.getType().isEmpty()) {
            setType(encounter.getType().get(0).getTextSimple());
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
