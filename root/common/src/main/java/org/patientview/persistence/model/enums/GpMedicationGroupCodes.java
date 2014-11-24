package org.patientview.persistence.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/11/2014
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GpMedicationGroupCodes {

    ECS("ECS");

    private String name;
    GpMedicationGroupCodes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
