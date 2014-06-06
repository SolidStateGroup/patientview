package org.patientview.persistence.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "PV_User_Group_Role")
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

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }
}
