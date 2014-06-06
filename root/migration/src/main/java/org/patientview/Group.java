package org.patientview;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */

public class Group extends AuditModel {

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    /*TODO http://docs.jboss.org/hibernate/orm/4.1/manual/en-US/html/ch06.html#types-registry */
    @Column(name = "fhir_resource_id")
    @org.hibernate.annotations.Type(type="pg-uuid")
    private UUID fhirResourceId;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup groupType;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UUID getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(final UUID fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public Lookup getGroupType() {
        return groupType;
    }

    public void setGroupType(final Lookup groupType) {
        this.groupType = groupType;
    }
}
