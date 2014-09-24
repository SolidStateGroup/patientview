package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class MessageReadReceipt {

    private User user;
    private Date created;

    public MessageReadReceipt() {

    }

    public MessageReadReceipt(org.patientview.persistence.model.MessageReadReceipt readReceipt) {
        setUser(new User(readReceipt.getUser(), null));
        setCreated(readReceipt.getCreated());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
