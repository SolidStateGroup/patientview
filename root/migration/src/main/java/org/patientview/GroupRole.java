package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Object to link an user to Group and Roles.
 *
 * Object should only have one noun this has 2. Maybe for another day.
 * UserLink?
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
public class GroupRole extends RangeModel {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne
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
}
