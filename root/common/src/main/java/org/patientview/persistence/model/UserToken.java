package org.patientview.persistence.model;

import org.apache.commons.lang3.StringUtils;
import org.patientview.persistence.model.enums.UserTokenTypes;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Entity
@Table(name = "pv_user_token")
public class UserToken extends BaseModel {

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token")
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration_date")
    private Date expiration;

    // todo: not required?
    @OneToOne(optional = true)
    @JoinColumn(name = "parent_token_id")
    private UserToken userToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date created;

    @Column(name = "check_secret_word")
    private boolean checkSecretWord;

    @Column(name = "secret_word_token")
    private String secretWordToken;

    @Column(name = "rate_limit", columnDefinition = "numeric", precision = 19, scale = 2)
    private Double rateLimit;

    @Column(name = "secret_word_indexes")
    private String secretWordIndexes;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private UserTokenTypes type = UserTokenTypes.WEB;

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

    public boolean isCheckSecretWord() {
        return checkSecretWord;
    }

    public void setCheckSecretWord(boolean checkSecretWord) {
        this.checkSecretWord = checkSecretWord;
    }

    public String getSecretWordToken() {
        return secretWordToken;
    }

    public void setSecretWordToken(String secretWordToken) {
        this.secretWordToken = secretWordToken;
    }

    public Double getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Double rateLimit) {
        this.rateLimit = rateLimit;
    }

    public List<String> getSecretWordIndexes() {
        if (StringUtils.isNotBlank(secretWordIndexes)) {
            return new ArrayList<>(Arrays.asList((secretWordIndexes.split(","))));
        }
        return null;
    }

    public void setSecretWordIndexes(List<String> secretWordIndexes) {
        if (null != secretWordIndexes && !secretWordIndexes.isEmpty()) {
            this.secretWordIndexes = StringUtils.join(secretWordIndexes, ",");
        }
    }

    public UserTokenTypes getType() {
        return type;
    }

    public void setType(UserTokenTypes type) {
        this.type = type;
    }
}
