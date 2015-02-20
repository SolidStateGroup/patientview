package org.patientview.persistence.model.enums;

/**
 * Enum for Conversation labels, used for User specific labels on ConversationUserLabel.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 19/02/2015
 */
public enum ConversationLabel {
    ARCHIVED("Archived"),
    INBOX("Inbox");

    private String name;
    ConversationLabel(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
