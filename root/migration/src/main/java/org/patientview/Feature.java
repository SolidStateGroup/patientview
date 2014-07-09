package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Feature extends RangeModel {

    @Column(name = "feature_name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "feature")
    private Set<Route> routes;

    @JsonIgnore
    @OneToMany(mappedBy = "feature")
    private Set<UserFeature> userFeatures;

    @JsonIgnore
    @OneToMany(mappedBy = "feature")
    private Set<GroupFeature> groupFeatures;

    @JsonIgnore
    @ManyToMany
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

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(final Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    public Set<Lookup> getFeatureTypes() {
        return featureTypes;
    }

    public void setFeatureTypes(Set<Lookup> featureTypes) {
        this.featureTypes = featureTypes;
    }
}
