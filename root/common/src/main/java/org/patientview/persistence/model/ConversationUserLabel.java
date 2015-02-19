package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.ConversationLabel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * For User specific labels on Conversations.
 * 
 * Created by jamesr@solidstategroup.com
 * Created on 19/02/2015
 */
@Entity
@Table(name = "pv_conversation_user_label")
public class ConversationUserLabel extends AuditModel {

    @OneToOne
    @JoinColumn(name = "conversation_user_id")
    private ConversationUser conversationUser;

    @Column(name = "conversation_label")
    @Enumerated(EnumType.STRING)
    private ConversationLabel conversationLabel;

    public ConversationUser getConversationUser() {
        return conversationUser;
    }

    public void setConversationUser(ConversationUser conversationUser) {
        this.conversationUser = conversationUser;
    }

    public ConversationLabel getConversationLabel() {
        return conversationLabel;
    }

    public void setConversationLabel(ConversationLabel conversationLabel) {
        this.conversationLabel = conversationLabel;
    }
}
