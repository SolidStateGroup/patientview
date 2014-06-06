package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class GroupFeature extends RangeModel {

    @OneToMany
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private Set<Group> groups;

    @OneToOne
    @JoinColumn(name = "feature_id")
    private Feature feature;

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(final Set<Group> groups) {
        this.groups = groups;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(final Feature feature) {
        this.feature = feature;
    }
}
