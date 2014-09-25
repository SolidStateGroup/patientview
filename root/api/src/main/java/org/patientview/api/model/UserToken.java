package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/09/2014
 */
public class UserToken {

    private User user;
    private String token;
    private Date expiration;
    private Date created;

    public UserToken () {

    }

    public UserToken (org.patientview.persistence.model.UserToken userToken) {
        if (userToken.getUser() != null) {
            setUser(new User(userToken.getUser(), null));
        }
        setToken(userToken.getToken());
        setExpiration(userToken.getExpiration());
        setCreated(userToken.getCreated());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
