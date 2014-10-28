package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
public enum ConversationTypes {
    MESSAGE("Message"),
    FEEDBACK("Feedback"),
    SHARED_THOUGHT("Shared Thought"),
    CONTACT_UNIT("Contact Unit");

    private String name;
    ConversationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
