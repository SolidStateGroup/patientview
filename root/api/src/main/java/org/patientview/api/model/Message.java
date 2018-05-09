package org.patientview.api.model;

import org.apache.commons.beanutils.BeanUtils;
import org.patientview.persistence.model.enums.MessageTypes;

import java.lang.reflect.InvocationTargetException;
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
    private MyMedia myMedia;
    private Boolean hasAttachment;
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
        setHasAttachment(message.getHasAttachment());
        setReadReceipts(new HashSet<MessageReadReceipt>());

        if (message.getReadReceipts() != null) {
            for (org.patientview.persistence.model.MessageReadReceipt readReceipt : message.getReadReceipts()) {
                getReadReceipts().add(new MessageReadReceipt(readReceipt));
            }
        }
        try {
            if (message.getMyMedia() != null) {
                setMyMedia(message.getMyMedia());
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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

    public Boolean getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public MyMedia getMyMedia() {
        return myMedia;
    }

    public void setMyMedia(org.patientview.persistence.model.MyMedia myMedia) throws InvocationTargetException,
            IllegalAccessException {
        this.myMedia = new org.patientview.api.model.MyMedia(myMedia);
    }
}
