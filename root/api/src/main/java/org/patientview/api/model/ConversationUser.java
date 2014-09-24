package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class ConversationUser {

    private User user;
    private Boolean anonymous;

    public ConversationUser () {

    }

    public ConversationUser (org.patientview.persistence.model.ConversationUser conversationUser) {
        setUser(new User(conversationUser.getUser(), null));
        setAnonymous(conversationUser.getAnonymous());
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
}
