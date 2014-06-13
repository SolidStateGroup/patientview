package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_group")
public class Group extends AuditModel {

    @Column(name = "group_name")
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

    @OneToMany(mappedBy = "group")
    private Set<GroupFeature> groupFeatures;

    @OneToMany(mappedBy = "group")
    private Set<Route> routes;

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

    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }
}
