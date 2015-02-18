package org.patientview.api.model;

import java.util.Date;

/**
 * MessageReadReceipt, used when marking Messages as read by a User.
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class MessageReadReceipt {

    private Long id;
    private BaseUser user;
    private Date created;

    public MessageReadReceipt() {
    }

    public MessageReadReceipt(org.patientview.persistence.model.MessageReadReceipt readReceipt) {
        setId(readReceipt.getId());
        setUser(new BaseUser(readReceipt.getUser()));
        setCreated(readReceipt.getCreated());
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
