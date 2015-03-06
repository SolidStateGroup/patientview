package org.patientview.api.model;

import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * ConversationUser, representing the link between a Conversation and a User, including if the User wishes to remain
 * anonymous.
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class ConversationUser {

    private Long id;
    private BaseUser user;
    private Boolean anonymous;
    private Set<ConversationUserLabel> conversationUserLabels;

    public ConversationUser() {
    }

    public ConversationUser(org.patientview.persistence.model.ConversationUser conversationUser) {
        setId(conversationUser.getId());
        setUser(new BaseUser(conversationUser.getUser()));
        setAnonymous(conversationUser.getAnonymous());

        setConversationUserLabels(new HashSet<ConversationUserLabel>());
        if (!CollectionUtils.isEmpty(conversationUser.getConversationUserLabels())) {
            for (org.patientview.persistence.model.ConversationUserLabel conversationUserLabel
                    : conversationUser.getConversationUserLabels()) {
                getConversationUserLabels().add(new ConversationUserLabel(conversationUserLabel));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
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
}
