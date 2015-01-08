package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/11/2014
 * For use in foot and eye checkup data
 */
public enum BodySites {
    LEFT_FOOT("Left foot"),
    RIGHT_FOOT("Right foot"),
    LEFT_EYE("Left eye"),
    RIGHT_EYE("Right eye");

    private String name;
    BodySites(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
