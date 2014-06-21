package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Collections;
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

    @Column(name = "email")
    private String email;

    @Column(name = "fullname")
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<GroupRole> groupRoles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<UserFeature> userFeatures;

   // @Type(type="pg-uuid")
    @Column(name = "fhir_resource_id")
    private UUID fhirResourceId;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

}
