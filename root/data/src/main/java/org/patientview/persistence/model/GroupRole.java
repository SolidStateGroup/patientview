package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
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

    //TODO - sort order just for the front end Enum sprint 2
    @Override
    public int compareTo(Object g1) {

        if (g1 == null) {
            return 0;
        }

        String thisType = this.getGroup().getGroupType().getValue();
        String objectType = this.getGroup().getGroupType().getValue();

        if (objectType.equalsIgnoreCase("SPECIALTY")) {
            return -1;
        } else if (thisType.equalsIgnoreCase("SPECIALTY")) {
            return 1;
        } else if (thisType.equalsIgnoreCase("UNIT") && objectType.equalsIgnoreCase("DISEASE_GROUP")) {
            return 1;
        } else if (thisType.equalsIgnoreCase("DISEASE_GROUP") && objectType.equalsIgnoreCase("UNIT")) {
            return -1;
        } else {
            return 0;
        }

    }
}
