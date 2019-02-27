package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
public enum ExternalServices {
    RDC_GROUP_ROLE_NOTIFICATION("RDC GroupRole Notification Service"),
    SURVEY_NOTIFICATION("Survey Notification Service");

    private String name;
    ExternalServices(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
