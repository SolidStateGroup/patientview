package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/07/2014
 */
public class Identifier extends RangeModel {

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup identifierType;

    @Column(name = "identifier")
    private String identifier;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lookup getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(Lookup identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
