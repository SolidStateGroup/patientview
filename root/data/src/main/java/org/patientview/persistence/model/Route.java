package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Entity
@Table(name = "pv_route")
public class Route extends SimpleAuditModel {

    @OrderColumn
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "template_url", nullable = false)
    private String templateUrl;

    @Column(name = "controller", nullable = false)
    private String controller;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToOne
    @JoinColumn(name = "type_id", nullable = false)
    private Lookup lookup;

    @OneToMany(mappedBy = "route")
    private Set<RouteLink> routeLink;

    public Set<RouteLink> getRouteLink() {
        return routeLink;
    }

    public void setRouteLink(final Set<RouteLink> routeLink) {
        this.routeLink = routeLink;
    }

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
