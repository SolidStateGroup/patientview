package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * TODO Sprint 3 - this might need to be done fixed with inheritance
 *
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */
@Entity
@Table(name = "pv_lookup_value")
public class StatisticType extends GenericLookup {

    @Column(name = "value")
    @Enumerated(EnumType.STRING)
    private org.patientview.persistence.model.enums.StatisticType value;

    public org.patientview.persistence.model.enums.StatisticType getValue() {
        return value;
    }

    public void setValue(final org.patientview.persistence.model.enums.StatisticType value) {
        this.value = value;
    }

}
