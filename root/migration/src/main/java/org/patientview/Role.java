package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
public class Role extends AuditModel {

    @Column(name = "role_name")
    private String name;

    @Column(name = "level")
    private Integer level;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    private Set<Route> routes;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup roleType;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    private Set<GroupRole> groupRoles;


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

    public Lookup getRoleType() {
        return roleType;
    }

    public void setRoleType(final Lookup roleType) {
        this.roleType = roleType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(final Integer level) {
        this.level = level;
    }
}
