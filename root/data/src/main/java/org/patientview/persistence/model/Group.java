package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
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
    //@Type(type="pg-uuid")
    private UUID fhirResourceId;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup groupType;

    @OneToMany(mappedBy = "group")
    private Set<GroupFeature> groupFeatures;

    @OneToMany(mappedBy = "group")
    private Set<GroupRole> groupRoles;

    @OneToMany(mappedBy = "group")
    private Set<Route> routes;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
    private Set<Link> links;

    @ManyToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name="PV_Group_Relationship",
            joinColumns = @JoinColumn(name="Child_Id", referencedColumnName="Id"),
            inverseJoinColumns = @JoinColumn(name="Parent_Id", referencedColumnName="Id"))
    @JsonIgnore
    @JsonManagedReference
    private Set<Group> parentGroups = new HashSet<Group>();

    @ManyToMany(mappedBy="parentGroups", cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    @JsonBackReference
    private Set<Group> childGroups = new HashSet<Group>();

    @Transient
    private Set<Group> parents = new HashSet<Group>();

    @Transient
    private Set<Group> children = new HashSet<Group>();

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

    @JsonIgnore
    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    @JsonIgnore
    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(final Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Set<Group> getParentGroups() {
        return parentGroups;
    }

    public void setParentGroups(Set<Group> parentGroups) {
        this.parentGroups = parentGroups;
    }

    public Set<Group> getChildGroups() {
        return childGroups;
    }

    public void setChildGroups(Set<Group> childGroups) {
        this.childGroups = childGroups;
    }

    public Set<Group> getParents() {
        return parents;
    }

    public void setParents(Set<Group> parents) {
        this.parents = parents;
    }

    public Set<Group> getChildren() {
        return children;
    }

    public void setChildren(Set<Group> children) {
        this.children = children;
    }
}
