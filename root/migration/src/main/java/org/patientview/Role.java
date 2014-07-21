package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.enums.Roles;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private Roles name;

    @Column(name = "level")
    private Integer level;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    private Set<Route> routes;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "type_id")
    private RoleType roleType;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    private Set<GroupRole> groupRoles;

    private Boolean visible;

    public Roles getName() {
        return name;
    }

    public void setName(final Roles name) {
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

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(final RoleType roleType) {
        this.roleType = roleType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(final Integer level) {
        this.level = level;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }
}
