package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
public enum MessageTypes {
    MESSAGE("Message"),
    MEDIA("Media"),
    FEEDBACK("Feedback"),
    SHARED_THOUGHT("Shared Thought"),
    CONTACT_UNIT("Contact Unit"),
    MEMBERSHIP_REQUEST("Membership Request");

    private String name;
    MessageTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
