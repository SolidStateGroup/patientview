package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.UserFeature;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class User extends BaseModel{

    private String forename;
    private String surname;
    private String email;
    private Set<UserFeature> userFeatures = new HashSet<>();
    private Set<GroupRole> groupRoles = new HashSet<>();
    private Date lastLogin;

    public User() {
    }

    public User(org.patientview.persistence.model.User user) {
        setId(user.getId());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setEmail(user.getEmail());
        setUserFeatures(user.getUserFeatures());
        setLastLogin(user.getLastLogin());

        for (org.patientview.persistence.model.GroupRole groupRole : user.getGroupRoles()) {
            getGroupRoles().add(new GroupRole(groupRole));
        }
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
