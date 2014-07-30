package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
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

    @Column(name = "sftp_user")
    private String sftpUser;

    @Column(name = "fhir_resource_id")
    private UUID fhirResourceId;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "visible_to_join")
    private Boolean visibleToJoin;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "address_3")
    private String address3;

    @Column(name = "postcode")
    private String postcode;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_id")
    private Lookup groupType;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private Set<GroupFeature> groupFeatures;

    @OneToMany(mappedBy = "group")
    private Set<GroupRole> groupRoles;

    @OneToMany(mappedBy = "group")
    private Set<RouteLink> routeLinks;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private Set<Link> links;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private Set<Location> locations;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ContactPoint> contactPoints;

    @Transient
    private Set<Group> parentGroups;

    @Transient
    private Set<Group> childGroups;

    @OneToMany(mappedBy = "sourceGroup", fetch = FetchType.EAGER)
    private Set<GroupRelationship> groupRelationships;

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

    public String getSftpUser() {
        return sftpUser;
    }

    public void setSftpUser(final String sftpUser) {
        this.sftpUser = sftpUser;
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
    public Set<RouteLink> getRouteLinks() {
        return routeLinks;
    }

    public void setRouteLinks(final Set<RouteLink> routeLinks) {
        this.routeLinks = routeLinks;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public Set<ContactPoint> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(Set<ContactPoint> contactPoints) {
        this.contactPoints = contactPoints;
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

    @JsonIgnore
    public Set<GroupRelationship> getGroupRelationships() {
        return groupRelationships;
    }

    public void setGroupRelationships(final Set<GroupRelationship> groupRelationships) {
        this.groupRelationships = groupRelationships;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }

    public Boolean getVisibleToJoin() {
        return visibleToJoin;
    }

    public void setVisibleToJoin(Boolean visibleToJoin) {
        this.visibleToJoin = visibleToJoin;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
