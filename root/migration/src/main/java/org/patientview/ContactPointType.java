package org.patientview;

import org.patientview.enums.ContactPointTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/07/2014
 */
@Entity
@Table(name = "pv_lookup_value")
public class ContactPointType extends GenericLookup {

    @Column(name = "value")
    @Enumerated(EnumType.STRING)
    private ContactPointTypes value;

    public ContactPointTypes getValue() {
        return value;
    }

    public void setValue(final ContactPointTypes value) {
        this.value = value;
    }
}
