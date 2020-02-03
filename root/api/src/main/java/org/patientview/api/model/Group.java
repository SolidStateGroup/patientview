package org.patientview.api.model;

import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Group, representing Group data and inheriting from the basic information in BaseGroup.
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class Group extends BaseGroup {

    private Set<GroupFeature> groupFeatures;
    private List<Group> parentGroups;
    private List<Group> childGroups;
    private List<Link> links;
    private Set<Location> locations;

    public Group() {
    }

    public Group(org.patientview.persistence.model.Group group) {

        this.groupFeatures = new HashSet<>();
        this.parentGroups = new ArrayList<>();
        this.childGroups = new ArrayList<>();
        this.links = new ArrayList<>();
        this.locations = new HashSet<>();

        setCode(group.getCode());
        setId(group.getId());
        setGroupType(group.getGroupType());
        setName(group.getName());
        setShortName(group.getShortName());
        setVisible(group.getVisible());
        setVisibleToJoin(group.getVisibleToJoin());
        setNoDataFeed(group.getNoDataFeed());

        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            setGroupFeatures(group.getGroupFeatures());
        }

        if (!CollectionUtils.isEmpty(group.getLocations())) {
            setLocations(group.getLocations());
        }

        // only need parent groups in front end for headers
        if (!CollectionUtils.isEmpty(group.getParentGroups())) {
            for (org.patientview.persistence.model.Group parentGroup : group.getParentGroups()) {
                Group newParentGroup = new Group();
                newParentGroup.setCode(parentGroup.getCode());
                newParentGroup.setId(parentGroup.getId());
                newParentGroup.setName(parentGroup.getName());
                getParentGroups().add(newParentGroup);
            }
        }

        if (!CollectionUtils.isEmpty(group.getLinks())) {
            for (Link link : group.getLinks()) {
                getLinks().add(link);
            }
        }

        setLastImportDate(group.getLastImportDate());
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
