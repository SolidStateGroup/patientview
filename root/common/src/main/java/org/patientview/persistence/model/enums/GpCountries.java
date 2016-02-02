package org.patientview.persistence.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GpCountries {

    ENG("England"),
    NI("Northern Island"),
    SCOT("Scotland");

    private String name;
    GpCountries(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
