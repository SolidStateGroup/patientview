package org.patientview.api.model;

import org.apache.commons.lang.StringUtils;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.UserFeature;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User, representing user information and extending from the basic user information in BaseUser.
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class User extends BaseUser {

    private String email;
    private Set<UserFeature> userFeatures = new HashSet<>();
    private Set<GroupRole> groupRoles = new HashSet<>();
    private Date lastLogin;
    private String lastLoginIpAddress;
    private Set<Identifier> identifiers;
    private Boolean locked;
    private Boolean emailVerified;
    private Boolean dummy;
    private String contactNumber;
    private Date created;
    private Boolean changePassword;

    // from fhirLink
    private Date latestDataReceivedDate;
    private BaseGroup latestDataReceivedBy;

    public User() {
    }

    public User(org.patientview.persistence.model.User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setEmail(user.getEmail());
        setUserFeatures(user.getUserFeatures());
        setLastLogin(user.getLastLogin());
        setLastLoginIpAddress(user.getLastLoginIpAddress());
        setLocked(user.getLocked());
        setEmailVerified(user.getEmailVerified());
        setDummy(user.getDummy());
        setContactNumber(user.getContactNumber());
        setCreated(user.getCreated());
        setChangePassword(user.getChangePassword());
        setDateOfBirth(user.getDateOfBirth());

        if (user.getGroupRoles() != null) {
            for (org.patientview.persistence.model.GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getGroup() != null) {
                    getGroupRoles().add(new GroupRole(groupRole));
                }
            }
        }

        setIdentifiers(user.getIdentifiers());
        setDeleted(user.getDeleted());
        setRoleDescription(user.getRoleDescription());

        // old method uses base64 to display image, doesn't work in ie8 so just return size as string
        if (StringUtils.isNotEmpty(user.getPicture())) {
            setPicture(Integer.toString(user.getPicture().length()));
        }
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

    public String getLastLoginIpAddress() {
        return lastLoginIpAddress;
    }

    public void setLastLoginIpAddress(String lastLoginIpAddress) {
        this.lastLoginIpAddress = lastLoginIpAddress;
    }

    public Set getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set identifiers) {
        this.identifiers = identifiers;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getDummy() {
        return dummy;
    }

    public void setDummy(Boolean dummy) {
        this.dummy = dummy;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public BaseGroup getLatestDataReceivedBy() {
        return latestDataReceivedBy;
    }

    public void setLatestDataReceivedBy(BaseGroup latestDataReceivedBy) {
        this.latestDataReceivedBy = latestDataReceivedBy;
    }

    public Date getLatestDataReceivedDate() {
        return latestDataReceivedDate;
    }

    public void setLatestDataReceivedDate(Date latestDataReceivedDate) {
        this.latestDataReceivedDate = latestDataReceivedDate;
    }

    public Boolean getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(Boolean changePassword) {
        this.changePassword = changePassword;
    }
}
