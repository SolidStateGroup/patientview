package org.patientview.api.model;

import org.patientview.persistence.model.enums.MessageTypes;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class Message {

    private MessageTypes type;
    private User user;
    private String message;
    private Date created;
    private Set<MessageReadReceipt> readReceipts;

    public Message() {

    }

    public Message(org.patientview.persistence.model.Message message) {
        setType(message.getType());
        setUser(new User(message.getUser(), null));
        setMessage(message.getMessage());
        setCreated(message.getCreated());

        setReadReceipts(new HashSet<MessageReadReceipt>());

        if (message.getReadReceipts() != null) {
            for (org.patientview.persistence.model.MessageReadReceipt readReceipt : message.getReadReceipts()) {
                getReadReceipts().add(new MessageReadReceipt(readReceipt));
            }
        }
    }

    public MessageTypes getType() {
        return type;
    }

    public void setType(MessageTypes type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Set<MessageReadReceipt> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(Set<MessageReadReceipt> readReceipts) {
        this.readReceipts = readReceipts;
    }
}
