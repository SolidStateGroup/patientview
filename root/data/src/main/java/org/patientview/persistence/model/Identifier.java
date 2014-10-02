package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/07/2014
 */
@Entity
@Table(name = "pv_identifier")
public class Identifier extends RangeModel {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup identifierType;

    @Column(name = "identifier")
    private String identifier;

    @OneToMany(mappedBy = "identifier")
    @JsonIgnore
    private List<FhirLink> fhirLink;

    @JsonIgnore
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

    public List<FhirLink> getFhirLink() {
        return fhirLink;
    }

    public void setFhirLink(List<FhirLink> fhirLink) {
        this.fhirLink = fhirLink;
    }
}
