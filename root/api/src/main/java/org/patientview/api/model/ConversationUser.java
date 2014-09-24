package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class ConversationUser {

    private Long id;
    private BaseUser user;
    private Boolean anonymous;

    public ConversationUser () {

    }

    public ConversationUser (org.patientview.persistence.model.ConversationUser conversationUser) {
        setId(conversationUser.getId());
        setUser(new BaseUser(conversationUser.getUser()));
        setAnonymous(conversationUser.getAnonymous());
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
}
