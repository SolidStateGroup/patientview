package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.ContactPointTypes;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/07/2014
 */
@Entity
@Table(name = "pv_lookup_value")
public class ContactPointType extends GenericLookup {


    @JsonIgnore
    @OneToMany(mappedBy = "contactPointType")
    private List<ContactPoint> contactPoints;

    @Column(name = "value")
    @Enumerated(EnumType.STRING)
    private ContactPointTypes value;

    public ContactPointTypes getValue() {
        return value;
    }

    public void setValue(final ContactPointTypes value) {
        this.value = value;
    }

    public List<ContactPoint> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(List<ContactPoint> contactPoints) {
        this.contactPoints = contactPoints;
    }
}
