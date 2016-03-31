package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 */
public enum EncounterTypes {
    TREATMENT("Treatment"),
    TRANSPLANT_STATUS("Transplant Status"),
    TRANSPLANT_STATUS_KIDNEY("Transplant Status (Kidney)"),
    TRANSPLANT_STATUS_PANCREAS("Transplant Status (Pancreas)"),
    SURGERY("Surgery");

    private String name;
    EncounterTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
