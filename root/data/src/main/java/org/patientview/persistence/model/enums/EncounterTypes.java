package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public enum EncounterTypes {
    TREATMENT("Treatment"),
    TRANSPLANT_STATUS("Transplant Status");

    private String name;
    EncounterTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
