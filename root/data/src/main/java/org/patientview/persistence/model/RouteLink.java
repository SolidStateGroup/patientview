package org.patientview.persistence.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Ideally this table would be split in three but for now this will do
 *
 * Created by james@solidstategroup.com
 * Created on 24/07/2014
 */
@Entity
@Table(name = "pv_route_link")
public class RouteLink extends SimpleAuditModel {

    @OneToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @OneToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @OneToOne
    @JoinColumn(name = "feature_id", nullable = true)
    private Feature feature;

    @OneToOne
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    public Route getRoute() {
        return route;
    }

    public void setRoute(final Route route) {
        this.route = route;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(final Feature feature) {
        this.feature = feature;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
