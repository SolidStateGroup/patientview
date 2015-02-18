package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

/**
 * GroupRole, representing a specific combination of Group and Role, used for User security permissions.
 * Created by jamesr@solidstategroup.com
 * Created on 28/08/2014
 */
public class GroupRole extends BaseModel {

    private Group group;
    private Role role;

    public GroupRole() {
    }

    public GroupRole(org.patientview.persistence.model.GroupRole groupRole) {
        setId(groupRole.getId());
        setGroup(new Group(groupRole.getGroup()));
        setRole(new Role(groupRole.getRole()));
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
