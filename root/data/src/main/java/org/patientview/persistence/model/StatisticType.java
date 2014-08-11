package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.StatisticTypes;

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
    private StatisticTypes value;

    public StatisticTypes getValue() {
        return value;
    }

    public void setValue(final StatisticTypes value) {
        this.value = value;
    }

}
