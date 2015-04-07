package org.patientview.persistence.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GroupTypes {

    UNIT("unit"), DISEASE_GROUP("Disease Group"), SPECIALTY("Specialty"), CENTRAL_SUPPORT("Central Support");

    private String name;
    GroupTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }


}
