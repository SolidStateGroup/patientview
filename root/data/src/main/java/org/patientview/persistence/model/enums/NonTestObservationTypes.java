package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 *
 * Observation types used to reference non test/result Observation records e.g. blood group, smoking history
 * Testcode.java contains other test codes generated from patientview.xsd e.g. ciclosporin, weight
 */
public enum NonTestObservationTypes {
    BLOOD_GROUP("Blood Group");

    private String name;
    NonTestObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
