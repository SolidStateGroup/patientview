package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
public class Route extends SimpleAuditModel {

    @Column(name = "display_order" )
    private Integer displayOrder;

    @Column(name = "url")
    private String url;

    @Column(name = "template_url")
    private String templateUrl;

    @Column(name = "controller")
    private String controller;

    @Column(name = "title")
    private String title;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup lookup;

    @OneToOne(optional = true)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne(optional = true)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(optional = true)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(final Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public void setTemplateUrl(final String templateUrl) {
        this.templateUrl = templateUrl;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void setLookup(final Lookup lookup) {
        this.lookup = lookup;
    }

    @JsonIgnore
    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    @JsonIgnore
    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }

    @JsonIgnore
    public Feature getFeature() {
        return feature;
    }

    public void setFeature(final Feature feature) {
        this.feature = feature;
    }

    @Override
    public int compareTo(Object object) {
        Route route;

        if (object == null) {
            return 1;
        } else {
            route = (Route) object;
        }

        if (route.getDisplayOrder() > this.getDisplayOrder()) {
            return -1;
        }

        if (route.getDisplayOrder() == this.getDisplayOrder()) {
            return 0;
        }
        if (route.getDisplayOrder() < this.getDisplayOrder()) {
            return 1;
        }
        return 0;
    }

}
