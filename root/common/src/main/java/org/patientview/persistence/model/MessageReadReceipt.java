package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/08/2014
 */
@Entity
@Table(name = "pv_message_read_receipt")
public class MessageReadReceipt extends BaseModel {

    @OneToOne
    @JoinColumn(name = "message_id")
    private Message message;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    public MessageReadReceipt() {
    }

    public MessageReadReceipt(Message message, User user) {
        this.message = message;
        this.user = user;
    }

    @JsonIgnore
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }
}
