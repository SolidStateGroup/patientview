package org.patientview.api.model;

import org.apache.commons.collections.CollectionUtils;
import org.patientview.persistence.model.Lookup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * BaseGroup, representing the minimum information required for Groups.
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class BaseGroup {

    private Long id;
    private String name;
    private String shortName;
    private String code;
    private Boolean visible;
    private Boolean visibleToJoin;
    private Boolean noDataFeed;
    private Lookup groupType;
    private Date lastImportDate;
    private List<String> parentCodes = new ArrayList<>();

    public BaseGroup() {
    }

    public BaseGroup(org.patientview.persistence.model.Group group) {
        setCode(group.getCode());
        setId(group.getId());
        setName(group.getName());
        setShortName(group.getShortName());
        setVisible(group.getVisible());
        setGroupType(group.getGroupType());
        setVisibleToJoin(group.getVisibleToJoin());
        setNoDataFeed(group.getNoDataFeed());
        setLastImportDate(group.getLastImportDate());

        // add parent group codes, used in UI to show or hide elements for specific specialties
        if (CollectionUtils.isNotEmpty(group.getParentGroups())) {
            for (org.patientview.persistence.model.Group parentGroup : group.getParentGroups()) {
                if (parentGroup.getCode() != null) {
                    parentCodes.add(parentGroup.getCode());
                }
            }
        }
    }

    public BaseGroup(org.patientview.api.model.Group group) {
        setCode(group.getCode());
        setId(group.getId());
        setName(group.getName());
        setShortName(group.getShortName());
        setVisible(group.getVisible());
        setGroupType(group.getGroupType());
        setVisibleToJoin(group.getVisibleToJoin());
        setNoDataFeed(group.getNoDataFeed());
        setLastImportDate(group.getLastImportDate());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getNoDataFeed() {
        return noDataFeed;
    }

    public void setNoDataFeed(Boolean noDataFeed) {
        this.noDataFeed = noDataFeed;
    }

    public Lookup getGroupType() {
        return groupType;
    }

    public void setGroupType(Lookup groupType) {
        this.groupType = groupType;
    }

    public Boolean getVisibleToJoin() {
        return visibleToJoin;
    }

    public void setVisibleToJoin(Boolean visibleToJoin) {
        this.visibleToJoin = visibleToJoin;
    }

    public Date getLastImportDate() {
        return lastImportDate;
    }

    public void setLastImportDate(Date lastImportDate) {
        this.lastImportDate = lastImportDate;
    }

    public List<String> getParentCodes() {
        return parentCodes;
    }

    public void setParentCodes(List<String> parentCodes) {
        this.parentCodes = parentCodes;
    }
}
