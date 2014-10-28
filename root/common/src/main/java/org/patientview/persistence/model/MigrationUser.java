package org.patientview.persistence.model;

import java.util.Date;
import java.util.Set;

/**
 * Transport object used for user migration
 *
 * Created by jamesr@solidstategroup.com
 * Created on 24/10/2014
 */
public class MigrationUser {

    // User
    private String username;
    private String password;
    private Boolean changePassword;
    private Boolean locked;
    private Boolean dummy;
    private Boolean emailVerified;
    private String verificationCode;
    private String email;
    private String forename;
    private Integer failedLogonAttempts;
    private String surname;
    private Set<GroupRole> groupRoles;
    private Set<UserFeature> userFeatures;
    private Set<Identifier> identifiers;
    private Date lastLogin;
    private String contactNumber;
    private Date created;

    public MigrationUser () {

    }

    public MigrationUser(User user) {
        username = user.getUsername();
        password = user.getPassword();
        changePassword = user.getChangePassword();
        locked = user.getLocked();
        dummy = user.getDummy();
        emailVerified = user.getEmailVerified();
        verificationCode = user.getVerificationCode();
        email = user.getEmail();
        forename = user.getForename();
        failedLogonAttempts = user.getFailedLogonAttempts();
        surname = user.getSurname();
        groupRoles = user.getGroupRoles();
        userFeatures = user.getUserFeatures();
        identifiers = user.getIdentifiers();
        lastLogin = user.getLastLogin();
        contactNumber = user.getContactNumber();
        created = user.getCreated();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(Boolean changePassword) {
        this.changePassword = changePassword;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getDummy() {
        return dummy;
    }

    public void setDummy(Boolean dummy) {
        this.dummy = dummy;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public Integer getFailedLogonAttempts() {
        return failedLogonAttempts;
    }

    public void setFailedLogonAttempts(Integer failedLogonAttempts) {
        this.failedLogonAttempts = failedLogonAttempts;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
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
}
