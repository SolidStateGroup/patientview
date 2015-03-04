package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 04/08/2014
 */
public enum RequestStatus {

    INCOMPLETE("Incomplete"), COMPLETED ("Completed"),
    IGNORED("Ignored"), DUPLICATE("Duplicate"), SUBMITTED("Submitted");

    private String name;
    RequestStatus(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
