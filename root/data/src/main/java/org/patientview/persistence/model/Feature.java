package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Entity
@Table(name = "pv_feature")
public class Feature extends RangeModel {

    @Column(name = "feature_name")
    private String name;

    @Column(name = "description")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
