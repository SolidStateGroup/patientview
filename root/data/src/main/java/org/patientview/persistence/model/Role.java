package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.Roles;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_role")
public class Role extends AuditModel implements GrantedAuthority {

    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private Roles name;

    @Column(name = "level")
    private Integer level;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    private Set<RouteLink> routeLinks;

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

    @JsonIgnore
    public Set<RouteLink> getRouteLinks() {
        return routeLinks;
    }

    public void setRouteLinks(final Set<RouteLink> routeLinks) {
        this.routeLinks = routeLinks;
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

    @Override
    public String getAuthority() {
        return getName().toString();
    }
}
