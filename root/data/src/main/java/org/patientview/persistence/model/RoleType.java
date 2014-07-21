package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.RoleTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */
@Entity
@Table(name = "pv_lookup_value")
public class RoleType extends GenericLookup {

    @Column(name = "value")
    @Enumerated(EnumType.STRING)
    private RoleTypes value;

    public RoleTypes getValue() {
        return value;
    }

    public void setValue(final RoleTypes value) {
        this.value = value;
    }
}
