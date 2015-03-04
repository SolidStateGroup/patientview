package org.patientview.persistence.model.enums;

/**
 * Used for join, forgotten credential requests, potentially for membership requests later on.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 04/03/2015
 */
public enum RequestTypes {
    JOIN_REQUEST("Join Request"),
    FORGOTTEN_CREDENTIALS("Forgotten Credentials");

    private String name;
    RequestTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
