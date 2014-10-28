package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
public enum UserInformationTypes {
    SHOULD_KNOW("Things people should know about me"),
    TALK_ABOUT("Things I'd like to talk about");

    private String name;
    UserInformationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
