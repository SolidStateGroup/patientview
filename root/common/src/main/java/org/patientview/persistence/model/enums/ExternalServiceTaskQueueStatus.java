package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
public enum ExternalServiceTaskQueueStatus {
    PENDING("Pending"),
    IN_PROGRESS("In progress"),
    FAILED("Failed");

    private String name;
    ExternalServiceTaskQueueStatus(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
