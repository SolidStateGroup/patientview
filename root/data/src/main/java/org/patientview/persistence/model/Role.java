package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "PV_Role")
public class Role extends AuditModel {

    @Column(name = "role_name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    private Set<Route> routes;

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
}
