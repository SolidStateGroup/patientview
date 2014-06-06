package org.patientview;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

/**
 * Main user class for the PatientView application
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
public class User extends RangeModel {

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

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "user")
    private List<GroupRole> groupRoles;

    /* http://docs.jboss.org/hibernate/orm/4.1/manual/en-US/html/ch06.html#types-registry */
    @Column(name = "fhir_resource_id")
    @org.hibernate.annotations.Type(type="pg-uuid")
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

    public List<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(final List<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }
}
