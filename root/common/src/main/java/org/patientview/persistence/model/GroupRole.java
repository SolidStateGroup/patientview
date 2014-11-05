package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Object to link an user to Group and Roles.
 *
 * Object should only have one noun this has 2. Maybe for another day.
 * UserLink?
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Entity
@Table(name = "pv_user_group_role")
public class GroupRole extends RangeModel implements GrantedAuthority {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // todo: FetchType.EAGER is required to get Group from GroupRole in api.Util methods else hibernate proxy exception
    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private Group group;

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    @JsonIgnore
    public String getAuthority() {
        return role!=null?getRole().getName().toString():null;
    }

}
