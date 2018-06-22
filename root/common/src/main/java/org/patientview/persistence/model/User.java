package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Main user class for the PatientView application
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends RangeModel implements UserDetails {

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(name = "salt")
    @JsonIgnore
    private String salt;

    @Column(name = "change_password")
    private Boolean changePassword;

    @Column(name = "failed_logon_attempts")
    private Integer failedLogonAttempts;

    @Column(name = "locked")
    private Boolean locked;

    @Column(name = "dummy")
    private Boolean dummy;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "email")
    private String email;

    @Column(name = "forename")
    private String forename;

    @Column(name = "surname")
    private String surname;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    
    // only for staff users
    @Column(name = "deleted")
    private Boolean deleted;
    
    // only for staff users
    @Column(name = "role_description")
    private String roleDescription;   
    
    // image data stored in base64
    @Column(name = "picture")
    private String picture;

    // secret word for multi-factor authentication
    @Column(name = "hide_secret_word_notification")
    private boolean hideSecretWordNotification = false;

    @Column(name = "secret_word")
    @JsonIgnore
    private String secretWord;

    @Transient
    private String name;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<UserInformation> userInformation;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<GroupRole> groupRoles;

    // EAGER for userHasStaffMessagingFeatures()
    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<UserFeature> userFeatures;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Identifier> identifiers;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private Set<ConversationUser> conversationUsers;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<UserObservationHeading> userObservationHeadings;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "last_login_ip_address")
    private String lastLoginIpAddress;

    @Column(name = "current_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date currentLogin;

    @Column(name = "current_login_ip_address")
    private String currentLoginIpAddress;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FhirLink> fhirLinks;

    @Transient
    private boolean canSwitchUser;

    @Transient
    private PatientManagement patientManagement;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password.trim();
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getSalt(){
        return salt;
    }

    public void setSalt(String salt){
        this.salt = salt;
    }

    public Boolean getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(final Boolean changePassword) {
        this.changePassword = changePassword;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(final Boolean locked) {
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

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(final String forename) {
        this.forename = forename;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public Set<UserInformation> getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(Set<UserInformation> userInformation) {
        this.userInformation = userInformation;
    }

    public void setGroupRoles(final Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(final Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    @JsonIgnore
    public Set<ConversationUser> getConversationUsers() {
        return conversationUsers;
    }

    public void setConversationUsers(Set<ConversationUser> conversationUsers) {
        this.conversationUsers = conversationUsers;
    }

    public String getName() {
        return forename + " " + surname;
    }

    //TODO User Detail fields need refactoring
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.<GrantedAuthority>emptyList();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled()
    {
        return true;
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

    public void setContactNumber(final String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Integer getFailedLogonAttempts() {
        return failedLogonAttempts;
    }

    public void setFailedLogonAttempts(final Integer failedLogonAttempts) {
        this.failedLogonAttempts = failedLogonAttempts;
    }

    public String getLastLoginIpAddress() {
        return lastLoginIpAddress;
    }

    public void setLastLoginIpAddress(String lastLoginIpAddress) {
        this.lastLoginIpAddress = lastLoginIpAddress;
    }

    public Set<FhirLink> getFhirLinks() {
        return fhirLinks;
    }

    public void setFhirLinks(final Set<FhirLink> fhirLinks) {
        this.fhirLinks = fhirLinks;
    }

    @JsonIgnore
    @PrePersist
    public void prePersist() {
        if (this.failedLogonAttempts == null) {
            this.failedLogonAttempts = 0;
        }
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Set<UserObservationHeading> getUserObservationHeadings() {
        return userObservationHeadings;
    }

    public void setUserObservationHeadings(Set<UserObservationHeading> userObservationHeadings) {
        this.userObservationHeadings = userObservationHeadings;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(Date currentLogin) {
        this.currentLogin = currentLogin;
    }

    public String getCurrentLoginIpAddress() {
        return currentLoginIpAddress;
    }

    public void setCurrentLoginIpAddress(String currentLoginIpAddress) {
        this.currentLoginIpAddress = currentLoginIpAddress;
    }

    public boolean isCanSwitchUser() {
        return canSwitchUser;
    }

    public void setCanSwitchUser(boolean canSwitchUser) {
        this.canSwitchUser = canSwitchUser;
    }

    public boolean isHideSecretWordNotification() {
        return hideSecretWordNotification;
    }

    public void setHideSecretWordNotification(boolean hideSecretWordNotification) {
        this.hideSecretWordNotification = hideSecretWordNotification;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public PatientManagement getPatientManagement() {
        return patientManagement;
    }

    public void setPatientManagement(PatientManagement patientManagement) {
        this.patientManagement = patientManagement;
    }
}
