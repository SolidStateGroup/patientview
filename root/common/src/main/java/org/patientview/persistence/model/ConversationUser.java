package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

/**
 * ConversationUser, for link between Conversation and User, also includes ConversationUserLabels and anonymous option.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Entity
@Table(name = "pv_conversation_user")
public class ConversationUser extends AuditModel {

    @OneToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "anonymous")
    private Boolean anonymous;

    @OneToMany(mappedBy = "conversationUser", 
            cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private Set<ConversationUserLabel> conversationUserLabels;

    @Transient
    private boolean canSwitchUser;

    @JsonIgnore
    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Set<ConversationUserLabel> getConversationUserLabels() {
        return conversationUserLabels;
    }

    public void setConversationUserLabels(Set<ConversationUserLabel> conversationUserLabels) {
        this.conversationUserLabels = conversationUserLabels;
    }

    public boolean isCanSwitchUser() {
        return canSwitchUser;
    }

    public void setCanSwitchUser(boolean canSwitchUser) {
        this.canSwitchUser = canSwitchUser;
    }
}
