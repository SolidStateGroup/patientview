package org.patientview.persistence.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GroupTypes {

    CENTRAL_SUPPORT("Central Support"),
    DISEASE_GROUP("Disease Group"),
    GENERAL_PRACTICE("General Practice"),
    SPECIALTY("Specialty"),
    UNIT("unit");

    private String name;
    GroupTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
