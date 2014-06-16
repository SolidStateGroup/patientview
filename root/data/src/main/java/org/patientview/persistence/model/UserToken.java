package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Entity
@Table(name = "pv_user_token")
public class UserToken extends BaseModel {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token")
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration_date")
    private Date expiration;

    @OneToOne(optional = true)
    @JoinColumn(name = "parent_token_id")
    private UserToken userToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date created;

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(final Date expiration) {
        this.expiration = expiration;
    }

    public UserToken getUserToken() {
        return userToken;
    }

    public void setUserToken(final UserToken userToken) {
        this.userToken = userToken;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }
}
