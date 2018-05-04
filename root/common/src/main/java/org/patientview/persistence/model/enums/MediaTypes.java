package org.patientview.persistence.model.enums;

/**
 * Created by toms@solidstategroup.com
 * Created on 20/03/2018
 */
public enum MediaTypes {
    IMAGE("image"),
    VIDEO("video");

    private String name;
    MediaTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
