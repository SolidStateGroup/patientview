package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Main user class for the PatientView application
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_user")
public class User extends RangeModel implements UserDetails {

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "change_password")
    private Boolean changePassword;

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

    @Transient
    private String name;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    //@SortNatural
    private Set<GroupRole> groupRoles;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<UserFeature> userFeatures;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Identifier> identifiers;

    @Column(name = "fhir_resource_id")
    private UUID fhirResourceId;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
    private Date lastLogin;

    @Column(name = "contact_number")
    private String contactNumber;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
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

    public UUID getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(final UUID fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
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
}
