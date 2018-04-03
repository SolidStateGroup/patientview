package org.patientview.api.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.enums.MessageTypes;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Message, representing messages that form the contents of a Conversation between Users.
 * Created by jamesr@solidstategroup.com
 * Created on 19/09/2014
 */
public class Message {

    private Long id;
    private MessageTypes type;
    private BaseUser user;
    private String message;
    private Long myMediaId;
    private Date created;
    private Set<MessageReadReceipt> readReceipts;

    public Message() {
    }

    public Message(org.patientview.persistence.model.Message message) {
        setId(message.getId());
        setType(message.getType());
        if (message.getUser() != null) {
            setUser(new BaseUser(message.getUser()));
        }
        setMessage(message.getMessage());
        setCreated(message.getCreated());

        setReadReceipts(new HashSet<MessageReadReceipt>());

        if (message.getReadReceipts() != null) {
            for (org.patientview.persistence.model.MessageReadReceipt readReceipt : message.getReadReceipts()) {
                getReadReceipts().add(new MessageReadReceipt(readReceipt));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageTypes getType() {
        return type;
    }

    public void setType(MessageTypes type) {
        this.type = type;
    }

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getMyMediaId() {
        return myMediaId;
    }

    public void setMyMediaId(Long myMediaId) {
        this.myMediaId = myMediaId;
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
