package org.patientview;

import org.patientview.enums.RoleTypes;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */

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
