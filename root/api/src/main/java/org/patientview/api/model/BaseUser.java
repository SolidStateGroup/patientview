package org.patientview.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.TimeZone;

/**
 * BaseUser, representing the minimum information required for Users.
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class BaseUser {

    private Long id;
    private String username;
    private String forename;
    private String surname;
    private Date dateOfBirth;

    // only staff users
    private Boolean deleted;

    // only staff users
    private String roleDescription;

    // picture, stored as base64 in database, but retrieved using separate call to User controller
    private String picture;

    private boolean canSwitchUser;

    public BaseUser() {
    }

    public BaseUser(org.patientview.persistence.model.User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setDateOfBirth(user.getDateOfBirth());
        setDeleted(user.getDeleted());
        setRoleDescription(user.getRoleDescription());
        if (StringUtils.isNotEmpty(user.getPicture())) {
            setPicture(Integer.toString(user.getPicture().length()));
        }

        setCanSwitchUser(user.isCanSwitchUser());
    }

    public BaseUser(org.patientview.api.model.User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setDateOfBirth(user.getDateOfBirth());
        setDeleted(user.getDeleted());
        setRoleDescription(user.getRoleDescription());
        if (StringUtils.isNotEmpty(user.getPicture())) {
            setPicture(Integer.toString(user.getPicture().length()));
        }

        setCanSwitchUser(user.isCanSwitchUser());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone = "Europe/London")
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public boolean isCanSwitchUser() {
        return canSwitchUser;
    }

    public void setCanSwitchUser(boolean canSwitchUser) {
        this.canSwitchUser = canSwitchUser;
    }
}
