package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "feature")
    private Set<Route> routes;

    @OneToMany(mappedBy = "feature")
    private Set<UserFeature> userFeatures;

    @OneToMany(mappedBy = "feature")
    private Set<GroupFeature> groupFeatures;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name="PV_Feature_Feature_Type",
            joinColumns = @JoinColumn(name="Feature_Id", referencedColumnName="Id"),
            inverseJoinColumns = @JoinColumn(name="Type_Id", referencedColumnName="Id"))
    private Set<Lookup> featureTypes = new HashSet<Lookup>();

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

    @JsonIgnore
    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }

    @JsonIgnore
    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(final Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    @JsonIgnore
    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    @JsonIgnore
    public Set<Lookup> getFeatureTypes() {
        return featureTypes;
    }

    public void setFeatureTypes(Set<Lookup> featureTypes) {
        this.featureTypes = featureTypes;
    }
}
