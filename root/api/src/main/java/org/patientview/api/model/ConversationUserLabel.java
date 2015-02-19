package org.patientview.api.model;

import org.patientview.persistence.model.enums.ConversationLabel;

/**
 * ConversationUserLabel, representing the link between a ConversationUser and a ConversationLabel.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 19/02/2014
 */
public class ConversationUserLabel {

    private ConversationLabel conversationLabel;

    public ConversationUserLabel() {
    }

    public ConversationUserLabel(org.patientview.persistence.model.ConversationUserLabel conversationUserLabel) {
        setConversationLabel(conversationUserLabel.getConversationLabel());
    }

    public ConversationLabel getConversationLabel() {
        return conversationLabel;
    }

    public void setConversationLabel(ConversationLabel conversationLabel) {
        this.conversationLabel = conversationLabel;
    }
}
