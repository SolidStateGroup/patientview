package org.patientview;

import javax.persistence.Column;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
public class Role extends AuditModel {

    @Column(name = "name")
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
