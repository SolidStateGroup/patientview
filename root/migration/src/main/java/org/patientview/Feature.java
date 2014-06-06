package org.patientview;

import javax.persistence.Column;
import javax.persistence.Enumerated;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Feature extends RangeModel {

    @Column(name = "feature_name")
    @Enumerated
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
