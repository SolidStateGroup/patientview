package org.patientview;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */

public class GroupRole extends RangeModel {

    @ManyToOne
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



}
