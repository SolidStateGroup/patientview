package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Used to store external standards for use by Codes, e.g. ICD-10 and SNOMED-CT
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
@Entity
@Table(name = "pv_external_standard")
public class ExternalStandard extends BaseModel {

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
