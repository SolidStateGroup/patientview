package org.patientview.api.model;

import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class Group extends BaseGroup {

    private Set<GroupFeature> groupFeatures = new HashSet<>();
    private List<Group> parentGroups = new ArrayList<>();
    private List<Group> childGroups = new ArrayList<>();
    private List<Link> links;
    private Set<Location> locations;

    public Group() {

    }

    public Group(org.patientview.persistence.model.Group group) {
        setCode(group.getCode());
        setId(group.getId());
        setGroupType(group.getGroupType());
        setGroupFeatures(group.getGroupFeatures());
        setName(group.getName());
        setShortName(group.getShortName());
        setVisible(group.getVisible());
        setParentGroups(new ArrayList<Group>());
        setLocations(group.getLocations());

        // only need parent groups in front end for headers
        for (org.patientview.persistence.model.Group parentGroup : group.getParentGroups()) {
            Group newParentGroup = new Group();
            newParentGroup.setCode(parentGroup.getCode());
            newParentGroup.setId(parentGroup.getId());
            newParentGroup.setName(parentGroup.getName());
            getParentGroups().add(newParentGroup);
        }

        setLinks(new ArrayList<Link>());
        if (group.getLinks() != null) {
            for (Link link : group.getLinks()) {
                getLinks().add(link);
            }
        }
    }

    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    public List<Group> getParentGroups() {
        return parentGroups;
    }

    public void setParentGroups(List<Group> parentGroups) {
        this.parentGroups = parentGroups;
    }

    public List<Group> getChildGroups() {
        return childGroups;
    }

    public void setChildGroups(List<Group> childGroups) {
        this.childGroups = childGroups;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }
}
